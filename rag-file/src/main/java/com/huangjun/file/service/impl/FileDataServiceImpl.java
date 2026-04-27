package com.huangjun.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huangjun.common.contants.RedisConstants;
import com.huangjun.common.domain.FileInfo;
import com.huangjun.common.utils.RedisUtils;
import com.huangjun.file.mapper.FileRedisMapper;
import com.huangjun.file.service.FileDataService;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class FileDataServiceImpl implements FileDataService {

    private FileRedisMapper fileRedisMapper;@Autowired
    public void setMappers(
            FileRedisMapper fileRedisMapper
    ){
        this.fileRedisMapper = fileRedisMapper;
    }


    @Override
    public void save(String fileName, String sessionId) {
        Date date = new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setSessionId(sessionId);
        fileInfo.setCreateTime(date);
        int result = fileRedisMapper.insert(fileInfo);
        if(result == 0){
            return;
        }
        RedisUtils.setFileInfoHash(RedisConstants.SESSION_FIND_FILENAME,sessionId,fileInfo);
    }

    @Override
    public FileInfo getBySessionId(String sessionId) {
        FileInfo fileInfo = RedisUtils.getHash(RedisConstants.SESSION_FIND_FILENAME, sessionId);
        if(fileInfo != null){
            return fileInfo;
        }
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<FileInfo>();
        queryWrapper.eq(FileInfo::getSessionId,sessionId);
        FileInfo fileInfoData = fileRedisMapper.selectOne(queryWrapper);
        if (fileInfoData==null){
            return null;
        }
        RedisUtils.setHash(RedisConstants.SESSION_FIND_FILENAME,sessionId,fileInfoData);
        return fileInfoData;
    }

    @Override
    public void deleteByFileName(String fileName) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getFileName,fileName);
        FileInfo fileInfoData = fileRedisMapper.selectOne(queryWrapper);
        if (fileInfoData==null){
            return;
        }
        RedisUtils.deleteHash(RedisConstants.SESSION_FIND_FILENAME,fileInfoData.getSessionId());
        fileRedisMapper.delete(queryWrapper);
    }
}
