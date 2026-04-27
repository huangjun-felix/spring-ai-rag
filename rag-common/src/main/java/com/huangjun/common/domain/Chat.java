package com.huangjun.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_chat")
public class Chat {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String sessionId;
    private Date date;
}
