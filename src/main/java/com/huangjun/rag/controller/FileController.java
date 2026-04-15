package com.huangjun.rag.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/file/rag")
public class FileController {
    private final ChatClient chatClient;
    @Autowired
    public FileController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/")
    public Flux<String> chat(String prompt, String sessionId){
        return chatClient.prompt(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID,sessionId))
                .advisors(a->a.param(
                        QuestionAnswerAdvisor.FILTER_EXPRESSION,"file_name == '测试'"
                ))
                .stream()
                .content();

    }

}
