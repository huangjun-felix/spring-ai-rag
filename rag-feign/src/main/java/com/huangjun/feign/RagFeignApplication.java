package com.huangjun.feign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RagFeignApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagFeignApplication.class, args);
    }
}
