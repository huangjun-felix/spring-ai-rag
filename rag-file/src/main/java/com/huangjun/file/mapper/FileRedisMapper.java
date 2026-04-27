package com.huangjun.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huangjun.common.domain.FileInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRedisMapper extends BaseMapper<FileInfo> {
}
