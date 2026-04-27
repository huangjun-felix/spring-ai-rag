package com.huangjun.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huangjun.common.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageDataMapper extends BaseMapper<ChatMessage> {
}
