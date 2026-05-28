package com.huangjun.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.huangjun.common","com.huangjun.file"})
@MapperScan("com.huangjun.file.mapper")
@EnableDiscoveryClient
@EnableAsync
@EnableFeignClients(basePackages = "com.huangjun.feign.feign")
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
