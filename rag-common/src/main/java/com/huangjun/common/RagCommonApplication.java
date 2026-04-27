package com.huangjun.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.huangjun.common"})
public class RagCommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagCommonApplication.class, args);
    }
}
