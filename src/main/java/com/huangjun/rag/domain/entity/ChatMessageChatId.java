package com.huangjun.rag.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_chat")
public class ChatMessageChatId {
    private Integer id;
    private String type;
    private String conversationId;
    private Date createTime;
}
