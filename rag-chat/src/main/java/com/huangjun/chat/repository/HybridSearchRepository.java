package com.huangjun.chat.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.huangjun.common.service.DocumentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class HybridSearchRepository {
    private static final Logger logger = LoggerFactory.getLogger(HybridSearchRepository.class);
    private static final int TOP_K = 10;

    private final ElasticsearchOperations elasticsearchOperations;
    private final VectorStore vectorStore;

    public HybridSearchRepository(ElasticsearchOperations elasticsearchOperations,
                                  @Qualifier("redisVectorStore") VectorStore vectorStore) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.vectorStore = vectorStore;
    }

    public List<Document> searchByEs(String query, String sessionId) {
        Query esQuery = new BoolQuery.Builder()
                .must(MultiMatchQuery.of(t -> t.fields("contents^1", "title^3").query(query).analyzer("ik_max_word")))
                .filter(TermQuery.of(f -> f.field("sessionId").value(sessionId)))
                .build()._toQuery();
        SearchHits<DocumentData> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(esQuery).withPageable(Pageable.ofSize(TOP_K)).build(),
                DocumentData.class);
        List<Document> docs = new ArrayList<>();
        for (SearchHit<DocumentData> hit : hits) {
            DocumentData d = hit.getContent();
            Map<String, Object> meta = new HashMap<>();
            meta.put("source", "es");
            meta.put("session_id", sessionId);
            meta.put("es_score", hit.getScore());
            docs.add(new Document(d.getTitle() + ": " + String.join("; ", d.getContents()), meta));
        }
        return docs;
    }

    public List<Document> searchWithParentChild(String query, String sessionId) {
        Filter.Expression childFilter = new FilterExpressionBuilder().and(
                new FilterExpressionBuilder().eq("doc_type", "child"),
                new FilterExpressionBuilder().eq("session_id", sessionId)).build();
        List<Document> childHits = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).filterExpression(childFilter).topK(TOP_K).similarityThreshold(0.5).build());
        if (childHits.isEmpty()) return Collections.emptyList();
        Set<String> parentIds = new LinkedHashSet<>();
        for (Document child : childHits) {
            Object pid = child.getMetadata().get("parent_id");
            if (pid != null) parentIds.add(pid.toString());
        }
        if (parentIds.isEmpty()) return Collections.emptyList();
        return searchParentsByIds(new ArrayList<>(parentIds), sessionId);
    }

    public List<Document> searchByEntity(String query, String sessionId) {
        Query entityQuery = new BoolQuery.Builder()
                .must(MultiMatchQuery.of(t -> t.fields("title^2", "contents").query(query).analyzer("ik_max_word")))
                .filter(TermQuery.of(f -> f.field("sessionId").value(sessionId)))
                .filter(TermQuery.of(f -> f.field("docType").value("entity")))
                .build()._toQuery();
        SearchHits<DocumentData> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(entityQuery).withPageable(Pageable.ofSize(20)).build(),
                DocumentData.class);
        if (hits.isEmpty()) return Collections.emptyList();
        Set<String> relatedParentIds = new LinkedHashSet<>();
        for (SearchHit<DocumentData> hit : hits) {
            if (hit.getContent().getParentId() != null)
                relatedParentIds.add(hit.getContent().getParentId());
        }
        if (relatedParentIds.isEmpty()) return Collections.emptyList();
        List<Document> parentDocs = searchParentsByIds(new ArrayList<>(relatedParentIds), sessionId);
        for (Document p : parentDocs) p.getMetadata().put("source", "graph_entity");
        logger.info("实体联想: 命中 {} 实体, 关联 {} 父块", hits.getTotalHits(), parentDocs.size());
        return parentDocs;
    }

    private List<Document> searchParentsByIds(List<String> parentIds, String sessionId) {
        Query esQuery = new BoolQuery.Builder()
                .should(parentIds.stream().map(id -> IdsQuery.of(i -> i.values(id))._toQuery()).toList())
                .filter(TermQuery.of(f -> f.field("sessionId").value(sessionId))).build()._toQuery();
        SearchHits<DocumentData> hits = elasticsearchOperations.search(
                NativeQuery.builder().withQuery(esQuery).withPageable(Pageable.ofSize(parentIds.size())).build(),
                DocumentData.class);
        List<Document> docs = new ArrayList<>();
        for (SearchHit<DocumentData> hit : hits) {
            DocumentData d = hit.getContent();
            String text = d.getFullText() != null ? d.getFullText() : d.getTitle() + ": " + String.join("; ", d.getContents());
            Map<String, Object> meta = new HashMap<>();
            meta.put("source", "es_parent");
            meta.put("session_id", sessionId);
            meta.put("title", d.getTitle());
            meta.put("parent_id", d.getId());
            docs.add(new Document(text, meta));
        }
        return docs;
    }
}
