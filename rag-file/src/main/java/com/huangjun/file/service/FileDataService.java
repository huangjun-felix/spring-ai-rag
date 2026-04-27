package com.huangjun.file.service;

import com.huangjun.common.domain.FileInfo;

public interface FileDataService {

    void save(String fileName,String sessionId);

    FileInfo getBySessionId(String sessionId);

    void deleteByFileName(String fileName);
}
