package com.huangjun.file.repository;

import com.google.common.collect.Lists;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PdfVectorRepository {

    private  VectorStore vectorStore;
    @Autowired
    public void init(@Qualifier("redisVectorStore") VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void savePdfVector(List<Document> documents) {
        List<List<Document>> partition = Lists.partition(documents, 10);
        for (List<Document> documentList : partition) {
            vectorStore.add(documentList);
        }
    }

    public List<Document> searchPdfVector(Resource resource, String sessionId){
        if(!StringUtils.hasText(sessionId)){
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        FilterExpressionBuilder finalBuilder = new FilterExpressionBuilder();
        Filter.Expression filterExpr;
        if(resource!=null && resource.exists() && StringUtils.hasText(resource.getFilename())){
            filterExpr = finalBuilder.and(
                    finalBuilder.eq("file_name",resource.getFilename()),
                    finalBuilder.eq("session_id",sessionId)
            ).build();
        }else {
            filterExpr = finalBuilder.eq("session_id",sessionId).build();
        }

        return vectorStore.similaritySearch(SearchRequest
                .builder()
                .similarityThreshold(0.7)
                .topK(2)
                .filterExpression(filterExpr)
                .build()
        );
    }
    
}
