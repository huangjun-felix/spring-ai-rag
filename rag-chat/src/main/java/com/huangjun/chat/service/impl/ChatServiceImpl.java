package com.huangjun.chat.service.impl;

import com.huangjun.chat.repository.ChatRepository;
import com.huangjun.chat.service.ChatService;
import com.huangjun.common.domain.Chat;
import com.huangjun.common.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChatServiceImpl implements ChatService {

    private ChatRepository chatRepository;
    @Autowired
    public void init(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void save(ChatMessage chatMessage) {
        Chat chat = new Chat();
        chat.setSessionId(chatMessage.getSessionId());
        chat.setDate(new Date());
        chatRepository.save(chat);
    }
}
