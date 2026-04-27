package com.huangjun.chat.service;

import com.huangjun.common.domain.Chat;
import com.huangjun.common.domain.ChatMessage;

public interface ChatService {

    void save(ChatMessage chatMessage);
}
