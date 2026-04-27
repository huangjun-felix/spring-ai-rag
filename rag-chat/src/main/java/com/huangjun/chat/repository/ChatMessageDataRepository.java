package com.huangjun.chat.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huangjun.chat.mapper.ChatMessageDataMapper;
import com.huangjun.common.contants.RedisConstants;
import com.huangjun.common.domain.ChatMessage;
import com.huangjun.common.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageDataRepository {

    private final ChatMessageDataMapper chatMessageDataMapper;

    public void save(ChatMessage chatMessage) {
        RedisUtils.chatMessagePush(RedisConstants.SESSION_MESSAGE+chatMessage.getSessionId(), Collections.singletonList(chatMessage));
        chatMessageDataMapper.insert(chatMessage);
    }

    public List<ChatMessage> selectMessageBySessionId(String conversationId) {
        List<ChatMessage> values = RedisUtils.chatMessageRange(RedisConstants.SESSION_MESSAGE + conversationId);
        if (values != null && !values.isEmpty()){
            return values;
        }
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMessage::getSessionId, conversationId);
        List<ChatMessage> chatMessages = chatMessageDataMapper.selectList(wrapper);
        if (chatMessages == null || chatMessages.isEmpty()) {
            return null;
        }
        RedisUtils.chatMessagePush(RedisConstants.SESSION_MESSAGE+conversationId,chatMessages);
        return chatMessages;
    }


}
