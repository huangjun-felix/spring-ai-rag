package com.huangjun.rag.domain.dto;

import com.huangjun.contentrabbot.domain.entity.ChatMessage;
import lombok.Data;

@Data
public class ChatMessageRecord {
    private ChatMessage chatUserMessage;
    private ChatMessage chatAssistantMessage;
}
