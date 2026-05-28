package com.huangjun.chat.controller;

import com.huangjun.chat.service.ChatMessageDataService;
import com.huangjun.chat.service.ChatService;
import com.huangjun.chat.service.HybridSearchService;
import com.huangjun.common.domain.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/ai")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatClient chatClient;
    private final HybridSearchService hybridSearchService;
    private final ChatService chatService;
    private final ChatMessageDataService chatMessageDataService;

    public ChatController(@Qualifier("ragClient") ChatClient chatClient,
                          HybridSearchService hybridSearchService,
                          ChatService chatService,
                          ChatMessageDataService chatMessageDataService) {
        this.chatClient = chatClient;
        this.hybridSearchService = hybridSearchService;
        this.chatService = chatService;
        this.chatMessageDataService = chatMessageDataService;
    }

    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public String chat(@RequestBody ChatMessage chatMessage) {
        String query = chatMessage.getMessage();
        String sessionId = chatMessage.getSessionId();

        // 步骤1：保存会话 + 用户消息
        chatMessage.setType("USER");
        chatMessage.setDate(new Date());
        chatService.save(chatMessage);
        chatMessageDataService.save(chatMessage);

        // 步骤2：混合检索
        List<Document> docs = hybridSearchService.hybridSearch(query, sessionId);
        logger.info("混合检索完成: {} 条文档", docs.size());

        String context = docs.isEmpty() ? "" : IntStream.range(0, docs.size())
                .mapToObj(i -> "[" + (i + 1) + "] " + docs.get(i).getText())
                .collect(Collectors.joining("\n\n"));

        // 步骤3：调用 LLM 生成回答
        String answer = chatClient.prompt()
                .system("你是一个知识库问答助手。\n【已知信息】\n" + (context.isEmpty() ? "无" : context)
                        + "\n\n【规则】仅根据已知信息回答；无法回答请说明；使用 [1] [2] 引用。")
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .call()
                .content();

        // 步骤4：异步保存 AI 回复
        saveAssistantMessage(sessionId, answer);

        return answer;
    }

    @Async("taskObAsync")
    void saveAssistantMessage(String sessionId, String answer) {
        try {
            ChatMessage sysMsg = new ChatMessage();
            sysMsg.setSessionId(sessionId);
            sysMsg.setMessage(answer);
            sysMsg.setType("ASSISTANT");
            sysMsg.setDate(new Date());
            chatMessageDataService.save(sysMsg);
        } catch (Exception e) {
            logger.error("保存 AI 回复失败", e);
        }
    }
}
