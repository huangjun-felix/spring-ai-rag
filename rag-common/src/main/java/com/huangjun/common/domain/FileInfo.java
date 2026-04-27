package com.huangjun.common.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_file_info")
public class FileInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String fileName;
    private String sessionId;
    private Date createTime;
}
