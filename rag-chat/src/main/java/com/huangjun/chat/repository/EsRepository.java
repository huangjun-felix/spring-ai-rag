package com.huangjun.chat.repository;

import com.huangjun.common.service.DocumentData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EsRepository extends ElasticsearchRepository<DocumentData, String> {

    default void insert(DocumentData documentData) {
        this.save(documentData);
    }

    default void batchInsert(java.util.List<DocumentData> documentDataList) {
        this.saveAll(documentDataList);
    }
}
