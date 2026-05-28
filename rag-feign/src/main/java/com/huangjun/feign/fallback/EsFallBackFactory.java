package com.huangjun.feign.fallback;

import com.huangjun.common.service.DocumentData;
import com.huangjun.feign.feign.EsFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

public class EsFallBackFactory implements FallbackFactory<EsFeign> {
    private static final Logger logger = LoggerFactory.getLogger(EsFallBackFactory.class);

    @Override
    public EsFeign create(Throwable cause) {
        logger.error("EsFeign 调用失败，启用降级", cause);
        return new EsFeign() {
            @Override
            public void insert(DocumentData d) {
                logger.warn("降级: ES 单条插入被跳过");
            }

            @Override
            public void batchInsert(java.util.List<DocumentData> list) {
                logger.warn("降级: ES 批量插入被跳过，共 {} 条", list.size());
            }
        };
    }
}
