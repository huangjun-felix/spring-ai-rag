package com.huangjun.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huangjun.chat.mapper.ChatMapper;
import com.huangjun.common.domain.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatRepository {

    private ChatMapper chatMapper;
    @Autowired
    public void init(ChatMapper chatMapper){
        this.chatMapper = chatMapper;
    }

    public void save(Chat chat){
        if (chat == null) return;
        chatMapper.insert(chat);
    }

    public boolean existsBySessionId(String sessionId) {
        if (sessionId == null) return false;
        LambdaQueryWrapper<Chat> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Chat::getSessionId, sessionId);
        return chatMapper.selectCount(wrapper) > 0;
    }

    public List<Chat> selectAll() {
        return chatMapper.selectList(null);
    }
}
