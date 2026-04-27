package com.huangjun.chat.service.impl;


import com.huangjun.chat.repository.ChatMessageDataRepository;
import com.huangjun.chat.service.ChatMessageDataService;
import com.huangjun.common.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageDataServiceImpl implements ChatMessageDataService {

    private final ChatMessageDataRepository chatMessageDataRepository;

    @Override
    public void save(ChatMessage chatMessage) {
        if(chatMessage==null){
            return;
        }
        chatMessageDataRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> selectMessageBySessionId(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) return List.of();
        return chatMessageDataRepository.selectMessageBySessionId(conversationId);
    }
}
