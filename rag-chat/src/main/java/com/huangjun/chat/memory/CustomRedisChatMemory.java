package com.huangjun.chat.memory;

import com.huangjun.chat.service.ChatMessageDataService;
import com.huangjun.common.contants.RedisConstants;
import com.huangjun.common.domain.ChatMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CustomRedisChatMemory implements ChatMemory {

    private  RedisTemplate<String, ChatMessage> redisTemplate;
    private ChatMessageDataService chatMessageService;

    @Autowired
    public void setBean(RedisTemplate<String, ChatMessage> redisTemplate,
                        ChatMessageDataService chatMessageService
    ) {
        this.redisTemplate = redisTemplate;
        this.chatMessageService = chatMessageService;
    }
    @Override
    @Async("chatInMemoryTaskExecutor")
    public void add(String conversationId, List<Message> messages) {
        String key = RedisConstants.SESSION_MESSAGE + conversationId;
        System.out.println("messages:"+messages);
        // 1. 【拆解】将 Spring AI 的复杂 Message 转换为我们简单的 DTO
        List<ChatMessage> dtoList = messages.stream().map(msg -> {
            return new ChatMessage(msg.getMessageType().getValue(), msg.getText());
        }).collect(Collectors.toList());
        System.out.println("dtoList:"+messages);
        // 2. 存入 Redis (这部分只会存极其干净的 JSON)
        redisTemplate.opsForList().rightPushAll(key, dtoList);
        redisTemplate.expire(key, 7, TimeUnit.HOURS);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = RedisConstants.SESSION_MESSAGE + conversationId;
        // 1. 从 Redis 取出简单的 DTO
        List<ChatMessage> dtoList = redisTemplate.opsForList().range(key, Integer.MIN_VALUE, -1);
        if (dtoList == null || dtoList.isEmpty()) {
            List<ChatMessage> chatMessageDTOS = chatMessageService.selectMessageBySessionId(conversationId);
            if (dtoList == null || dtoList.isEmpty()) return List.of();
            redisTemplate.opsForList().rightPushAll(key, chatMessageDTOS);
            redisTemplate.expire(key, 7, TimeUnit.HOURS);
            dtoList = chatMessageDTOS;
        }
        return dtoList.stream().map(dto -> {
            String type = dto.getType().toUpperCase();
            String message = dto.getMessage();

            // 根据保存的角色类型，重新 new 出对应的 Message 实现类
            if ("USER".equals(type)) {
                return new UserMessage(message);
            } else if ("ASSISTANT".equals(type)) {
                return new AssistantMessage(message);
            } else if ("SYSTEM".equals(type)) {
                return new SystemMessage(message);
            } else {
                // 默认兜底
                return new UserMessage(message);
            }
        }).collect(Collectors.toList());
    }
    @Override
    @Async("chatInMemoryTaskExecutor")
    public void clear(String conversationId) {
        redisTemplate.delete(RedisConstants.SESSION_MESSAGE + conversationId);
    }
}