package com.huangjun.chat.controller;

import com.huangjun.chat.repository.EsRepository;
import com.huangjun.common.service.DocumentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/es")
public class EsController {
    private static final Logger logger = LoggerFactory.getLogger(EsController.class);
    private final EsRepository esRepository;

    public EsController(EsRepository esRepository) {
        this.esRepository = esRepository;
    }

    @PostMapping("/insert")
    public void insert(@RequestBody DocumentData documentData) {
        if (documentData == null) return;
        esRepository.insert(documentData);
    }

    @PostMapping("/batchInsert")
    public void batchInsert(@RequestBody List<DocumentData> documentDataList) {
        if (documentDataList == null || documentDataList.isEmpty()) return;
        logger.info("ES 批量插入: {} 条", documentDataList.size());
        esRepository.saveAll(documentDataList);
    }
}
