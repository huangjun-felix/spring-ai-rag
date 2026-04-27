package com.huangjun.file.service.impl;

import com.huangjun.file.service.FileService;
import com.huangjun.file.service.PdfVectorService;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileServiceImpl implements FileService {

    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    @Value("${spring.minio.bucket-name:my-bucket}")
    private String bucketName;

    private  MinioClient minioClient;
    @Autowired
    public void init(@Qualifier("minioClient") MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void createBucketIfNotExists(){
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        }catch (Exception e){
              log.error(e.getMessage());
        }
    }
    @Override
    public String fileUpload(MultipartFile file) {
        try {
            createBucketIfNotExists();
            String fileOriginalName = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(fileOriginalName);
//            String substring = fileOriginalName.substring(fileOriginalName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-","") + extension;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
            return fileName;
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }
    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.HOURS)
                            .build()
            );
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public InputStream getFileObject(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }
}
