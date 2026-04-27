package com.huangjun.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RefreshScope
public class MinioConfig {

    @Value("${spring.minio.access-key:minioadmin}")
    private String accessKey;
    @Value("${spring.minio.secret-key:minioadmin}")
    private String secretKey;
    @Value("${spring.minio.endpoint:http://192.168.1.137:9000}")
    private String endpoint;

    @Bean("minioClient")
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey,secretKey)
                .build();
    }
}
