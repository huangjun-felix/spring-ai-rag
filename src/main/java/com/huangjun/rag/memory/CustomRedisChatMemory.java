package com.huangjun.rag.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CustomRedisChatMemory implements ChatMemory {

    public static final String MEMORY_KEY_PREFIX = "ai:chat:memory:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CustomRedisChatMemory(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    @Async("chatInMemoryTaskExecutor")
    public void add(String conversationId, List<Message> messages) {
        String key = MEMORY_KEY_PREFIX + conversationId;
        System.out.println("messages:"+messages);
        // 1. 【拆解】将 Spring AI 的复杂 Message 转换为我们简单的 DTO
//        List<Object> dtoList = messages.stream().map(msg -> {
//            return new Object(msg.getMessageType().getValue(), msg.getText());
//        }).collect(Collectors.toList());
        System.out.println("dtoList:"+messages);
        // 2. 存入 Redis (这部分只会存极其干净的 JSON)
        redisTemplate.opsForList().rightPushAll(key, messages);
        redisTemplate.expire(key, 7, TimeUnit.HOURS);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = MEMORY_KEY_PREFIX + conversationId;
        // 1. 从 Redis 取出简单的 DTO
        List<Object> dtoList = redisTemplate.opsForList().range(key, Integer.MIN_VALUE, -1);
//        if (dtoList == null || dtoList.isEmpty()) {
////            List<Object> chatMessageDTOS = chatMessageService.selectMessageByChatId(conversationId);
//            if (chatMessageDTOS == null || chatMessageDTOS.isEmpty()) return List.of();
//            redisTemplate.opsForList().rightPushAll(key, chatMessageDTOS);
//            redisTemplate.expire(key, 7, TimeUnit.HOURS);
////            dtoList = chatMessageDTOS;
//        }
//        return dtoList.stream().map(dto -> {
//            String type = dto.getMessageType().toUpperCase();
//            String content = dto.getContent();
//
//            // 根据保存的角色类型，重新 new 出对应的 Message 实现类
//            if ("USER".equals(type)) {
//                return new UserMessage(content);
//            } else if ("ASSISTANT".equals(type)) {
//                return new AssistantMessage(content);
//            } else if ("SYSTEM".equals(type)) {
//                return new SystemMessage(content);
//            } else {
//                // 默认兜底
//                return new UserMessage(content);
//            }
//        }).collect(Collectors.toList());
        return Collections.singletonList(new UserMessage(String.valueOf(dtoList)));
    }
    @Override
    @Async("chatInMemoryTaskExecutor")
    public void clear(String conversationId) {
        redisTemplate.delete(MEMORY_KEY_PREFIX + conversationId);
    }
}