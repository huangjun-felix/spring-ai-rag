package com.huangjun.rag.domain.dto;

import com.huangjun.contentrabbot.domain.entity.ChatMessage;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatMessageDTO implements Serializable {
    
    // 角色：通常是 "USER", "ASSISTANT", "SYSTEM"
    private String messageType; 
    // 真正的聊天文本内容
    private String content;

    // 必须保留无参构造函数，Jackson 才能反序列化
    public ChatMessageDTO() {
    }

    public ChatMessageDTO(String messageType, String content) {
        this.messageType = messageType;
        this.content = content;
    }

    public ChatMessageDTO(ChatMessage chatMessage) {
        if(chatMessage != null) {
            this.messageType = chatMessage.getType();
            this.content = chatMessage.getContent();
        }
    }

    // --- 下面是 Getter 和 Setter ---
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}