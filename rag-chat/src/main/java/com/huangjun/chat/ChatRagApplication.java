package com.huangjun.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.huangjun.common","com.huangjun.chat"})
@MapperScan("com.huangjun.chat.mapper")
@EnableDiscoveryClient
public class ChatRagApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatRagApplication.class, args);
    }
}
