package com.huangjun.feign.feign;

import com.huangjun.common.service.DocumentData;
import com.huangjun.feign.fallback.EsFallBackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "rag-chat", path = "/admin/es", fallbackFactory = EsFallBackFactory.class)
public interface EsFeign {

    @PostMapping("/insert")
    void insert(@RequestBody DocumentData documentData);

    @PostMapping("/batchInsert")
    void batchInsert(@RequestBody List<DocumentData> documentDataList);
}
