package com.huangjun.chat.service;

import com.huangjun.chat.repository.HybridSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class HybridSearchService {
    private static final Logger logger = LoggerFactory.getLogger(HybridSearchService.class);
    private static final double RRF_K = 60.0;
    private static final int RRF_TOP_K = 20;
    private static final int RERANK_TOP_K = 5;

    private final Executor taskScheduler;
    private final HybridSearchRepository hybridSearchRepository;
    private final ChatClient chatClient;

    public HybridSearchService(@Qualifier("taskAsync") Executor taskScheduler,
                               HybridSearchRepository hybridSearchRepository,
                               @Qualifier("ragClient") ChatClient chatClient) {
        this.taskScheduler = taskScheduler;
        this.hybridSearchRepository = hybridSearchRepository;
        this.chatClient = chatClient;
    }

    public List<Document> hybridSearch(String query, String sessionId) {
        CompletableFuture<List<Document>> esFuture = CompletableFuture.supplyAsync(
                () -> hybridSearchRepository.searchByEs(query, sessionId), taskScheduler);
        CompletableFuture<List<Document>> vectorFuture = CompletableFuture.supplyAsync(
                () -> hybridSearchRepository.searchWithParentChild(query, sessionId), taskScheduler);
        CompletableFuture<List<Document>> entityFuture = CompletableFuture.supplyAsync(
                () -> hybridSearchRepository.searchByEntity(query, sessionId), taskScheduler);

        List<Document> esResults = esFuture.join();
        List<Document> vectorResults = vectorFuture.join();
        List<Document> entityResults = entityFuture.join();
        logger.info("ES:{} 向量:{} 实体:{}", esResults.size(), vectorResults.size(), entityResults.size());

        List<Document> rrfDocs = rrfFusion(esResults, vectorResults, entityResults, RRF_TOP_K);
        if (rrfDocs.size() <= RERANK_TOP_K) return rrfDocs;
        return rerank(query, rrfDocs);
    }

    private List<Document> rrfFusion(List<Document> esDocs, List<Document> vectorDocs, List<Document> entityDocs, int topK) {
        Map<String, Document> docMap = new LinkedHashMap<>();
        Map<String, Double> scores = new HashMap<>();
        scoreDocs(esDocs, docMap, scores);
        scoreDocs(vectorDocs, docMap, scores);
        scoreDocs(entityDocs, docMap, scores);
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK).map(e -> { Document d = docMap.get(e.getKey()); d.getMetadata().put("rrf_score", e.getValue()); return d; })
                .toList();
    }

    private void scoreDocs(List<Document> docs, Map<String, Document> docMap, Map<String, Double> scores) {
        for (int i = 0; i < docs.size(); i++) {
            String key = docKey(docs.get(i));
            docMap.putIfAbsent(key, docs.get(i));
            scores.merge(key, 1.0 / (RRF_K + i + 1), Double::sum);
        }
    }

    private List<Document> rerank(String query, List<Document> docs) {
        List<CompletableFuture<RerankResult>> futures = docs.stream()
                .map(doc -> CompletableFuture.supplyAsync(() -> {
                    try { return new RerankResult(doc, rerankScore(query, doc.getText())); }
                    catch (Exception e) { return new RerankResult(doc, 5); }
                }, taskScheduler)).toList();
        return futures.stream().map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(RerankResult::score).reversed())
                .limit(RERANK_TOP_K).map(RerankResult::doc).toList();
    }

    private int rerankScore(String query, String chunk) {
        String prompt = String.format(
                "判断文档片段是否能回答用户问题。打分0-10，仅输出 JSON: {\"score\": 数字}\n用户问题：%s\n文档片段：%s",
                query, chunk.length() > 1500 ? chunk.substring(0, 1500) + "..." : chunk);
        String response = chatClient.prompt().user(prompt).call().content();
        if (response == null || response.isEmpty()) return 5;
        try {
            String num = response.replaceAll("[^0-9]", " ").trim().split("\\s+")[0];
            int s = Integer.parseInt(num);
            return (s >= 0 && s <= 10) ? s : 5;
        } catch (Exception e) { return 5; }
    }

    private String docKey(Document doc) {
        String text = doc.getText();
        return text.length() > 100 ? text.substring(0, 100) : text;
    }

    private record RerankResult(Document doc, int score) {}
}
