package com.huangjun.rag.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_chat_message")
public class ChatMessage {
    private Integer id;
    private String chatId;
    /**
     * type字段代表的是角色：user、assistant
     * */
    private String type;
    private String content;
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;
}
