package com.huangjun.chat.repository;

import com.huangjun.chat.mapper.ChatMapper;
import com.huangjun.common.domain.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class ChatRepository {

    private ChatMapper chatMapper;
    @Autowired
    public void init(ChatMapper chatMapper){
        this.chatMapper = chatMapper;
    }

    public void save(Chat chat){
        if (chat == null){
            return;
        }
        chatMapper.insert(chat);
    }

}
