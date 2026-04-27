package com.huangjun.chat.service;


import com.huangjun.common.domain.ChatMessage;

import java.util.List;

public interface ChatMessageDataService {

    void save(ChatMessage chatMessage);

    List<ChatMessage> selectMessageBySessionId(String conversationId);
}
