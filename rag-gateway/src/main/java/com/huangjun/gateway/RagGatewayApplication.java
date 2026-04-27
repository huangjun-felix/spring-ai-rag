package com.huangjun.gateway;

import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@SpringBootApplication(exclude = {
        WebMvcAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        OpenAiAudioSpeechAutoConfiguration.class,
        OpenAiChatAutoConfiguration.class
})
public class RagGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagGatewayApplication.class, args);
    }
}
