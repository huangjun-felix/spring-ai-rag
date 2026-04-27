package com.huangjun.feign.fallback;

import com.huangjun.feign.feign.FileFeign;
import org.springframework.cloud.openfeign.FallbackFactory;


public class FileFallBackFactory implements FallbackFactory<FileFeign> {
    @Override
    public FileFeign create(Throwable cause) {
        return null;
    }
}
