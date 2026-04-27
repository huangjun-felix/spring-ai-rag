package com.huangjun.chat.controller;

import com.huangjun.chat.service.ChatMessageDataService;
import com.huangjun.chat.service.ChatService;
import com.huangjun.common.domain.ChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class ChatController {
    private ChatClient chatClient;
    private ChatService chatService;
    private ChatMessageDataService chatMessageDataService;
    private VectorStore vectorStore;
    @Autowired
    public void setChatClient(@Qualifier("ragClient") ChatClient chatClient,
                              ChatService chatService,
                              ChatMessageDataService chatMessageDataService,
                              @Qualifier("redisVectorStore") VectorStore vectorStore
                              ) {
        this.chatClient = chatClient;
        this.chatService = chatService;
        this.chatMessageDataService = chatMessageDataService;
        this.vectorStore = vectorStore;
    }

    @PostMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public String chat(@RequestBody ChatMessage chatMessage){
//        System.out.println("当前传入的 sessionId: [" + chatMessage.getSessionId() + "]");
//
//        List<Document> docs = vectorStore.similaritySearch(
//                SearchRequest.builder()
//                        .query("项目经历是什么")
//                        .filterExpression("session_id == '" + chatMessage.getSessionId() + "'")
//                        .topK(5)
//                        .similarityThresholdAll()
//                        .build()
//        );
//
//        if (docs.isEmpty()) {
//            System.out.println("❌ 惨了，一条都没查出来！");
//        } else {
//            docs.forEach(doc -> System.out.println("✅ 查到了: " + doc.getText()));
//        }
//        return "共查到 " + docs.size() + " 条数据";
//        chatService.save(chatMessage);
//        chatMessageDataService.save(chatMessage);
        String message = chatClient.prompt()
                .user(chatMessage.getMessage())
                .advisors(a -> a.param(
                        ChatMemory.CONVERSATION_ID, chatMessage.getSessionId()
                ))
                .advisors(a -> a.param(
                        QuestionAnswerAdvisor.FILTER_EXPRESSION, "session_id == '" + chatMessage.getSessionId() + "'"
                ))
                .call()
//                .stream()
                .content();
//        ChatMessage chatMessage1 = new ChatMessage();
//        chatMessage1.setType(chatMessage.getType());
//        chatMessage1.setSort(chatMessage.getSort());
//        chatMessage1.setSessionId(chatMessage.getSessionId());
//        chatMessage1.setType("SYSTEM");
//        chatMessage1.setMessage(message);
//        chatMessageDataService.save(chatMessage1);
        return message;
    }

}
