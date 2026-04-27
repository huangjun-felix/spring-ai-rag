package com.huangjun.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String message;
    private String type;
    private Integer sort;
    private String sessionId;
    private Date date;

    public ChatMessage(String type, String message) {
        if (type != null) {
            this.type = type.toUpperCase();
        }else {
            this.type = "default";
        }
        this.message = Objects.requireNonNullElse(message, "");

    }
}
