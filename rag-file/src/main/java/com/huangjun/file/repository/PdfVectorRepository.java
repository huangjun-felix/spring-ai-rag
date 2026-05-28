package com.huangjun.file.repository;

import com.google.common.collect.Lists;
import com.huangjun.common.service.DocumentData;
import com.huangjun.feign.feign.EsFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;

@Repository
public class PdfVectorRepository {
    private static final Logger logger = LoggerFactory.getLogger(PdfVectorRepository.class);

    private final VectorStore vectorStore;
    private final EsFeign esFeign;

    public PdfVectorRepository(@Qualifier("redisVectorStore") VectorStore vectorStore, EsFeign esFeign) {
        this.vectorStore = vectorStore;
        this.esFeign = esFeign;
    }

    public void savePdfVector(List<Document> documents) {
        for (List<Document> batch : Lists.partition(documents, 10)) vectorStore.add(batch);
    }

    public void savePdfVectorWithGraph(List<Document> vectorDocs, List<DocumentData> parents,
                                       List<DocumentData> children, List<DocumentData> entities) {
        // ES: 父块 + 实体
        List<DocumentData> esBatch = new ArrayList<>(parents); esBatch.addAll(entities);
        for (List<DocumentData> batch : Lists.partition(esBatch, 10)) esFeign.batchInsert(batch);
        // 向量库: 父块
        for (List<Document> batch : Lists.partition(vectorDocs, 10)) vectorStore.add(batch);
        // 向量库: 子块
        for (List<DocumentData> batch : Lists.partition(children, 10)) {
            List<Document> childDocs = batch.stream().map(c -> {
                Map<String, Object> meta = new HashMap<>();
                meta.put("session_id", c.getSessionId()); meta.put("section_title", c.getTitle());
                meta.put("doc_type", "child"); meta.put("parent_id", c.getParentId());
                return new Document("【" + c.getTitle() + "】 " + String.join("；", c.getContents()), meta);
            }).toList();
            vectorStore.add(childDocs);
        }
        // 向量库: 实体
        if (!entities.isEmpty()) {
            for (List<DocumentData> batch : Lists.partition(entities, 10)) {
                List<Document> entityDocs = batch.stream().map(e -> {
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("session_id", e.getSessionId()); meta.put("doc_type", "entity");
                    meta.put("entity_type", e.getFullText()); meta.put("entity_name", e.getTitle());
                    meta.put("parent_id", e.getParentId());
                    return new Document("【实体:" + e.getFullText() + "】 " + e.getTitle(), meta);
                }).toList();
                vectorStore.add(entityDocs);
            }
        }
        logger.info("完整存储: 父块{} 子块{} 实体{}", parents.size(), children.size(), entities.size());
    }

    public List<Document> searchPdfVector(Resource resource, String sessionId) {
        if (!StringUtils.hasText(sessionId)) throw new IllegalArgumentException("sessionId 不能为空");
        return vectorStore.similaritySearch(SearchRequest.builder().similarityThreshold(0.7).topK(2)
                .filterExpression(new FilterExpressionBuilder().eq("session_id", sessionId).build()).build());
    }
}
