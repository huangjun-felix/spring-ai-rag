package com.huangjun.chat.controller;

import com.huangjun.chat.repository.ChatMessageDataRepository;
import com.huangjun.chat.repository.ChatRepository;
import com.huangjun.common.domain.Chat;
import com.huangjun.common.domain.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/common")
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

    private final ChatRepository chatRepository;
    private final ChatMessageDataRepository chatMessageDataRepository;

    @Autowired
    public CommonController(ChatRepository chatRepository, ChatMessageDataRepository chatMessageDataRepository) {
        this.chatRepository = chatRepository;
        this.chatMessageDataRepository = chatMessageDataRepository;
    }

    /**
     * 获取所有会话列表
     */
    @GetMapping("/sessions")
    public List<Chat> getSessions() {
        List<Chat> sessions = chatRepository.selectAll();
        logger.info("获取会话列表: {} 条记录", sessions.size());
        return sessions;
    }

    /**
     * 根据会话 ID 获取对话历史
     */
    @GetMapping("/messages/{sessionId}")
    public List<ChatMessage> getMessages(@PathVariable("sessionId") String sessionId) {
        List<ChatMessage> messages = chatMessageDataRepository.selectMessageBySessionId(sessionId);
        logger.info("获取消息历史: sessionId={}, {} 条记录", sessionId, messages == null ? 0 : messages.size());
        return messages;
    }
}
