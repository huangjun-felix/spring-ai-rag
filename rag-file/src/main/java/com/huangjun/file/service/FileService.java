package com.huangjun.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    String fileUpload(MultipartFile file);

    void deleteFile(String fileName);

    String getFileUrl(String fileName);

    InputStream getFileObject(String fileName);
}
