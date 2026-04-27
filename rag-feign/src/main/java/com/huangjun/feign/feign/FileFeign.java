package com.huangjun.feign.feign;

import com.huangjun.feign.fallback.FileFallBackFactory;
import org.springframework.ai.document.Document;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "rag-file",path = "/admin",fallbackFactory = FileFallBackFactory.class)
public interface FileFeign {

    @PostMapping("/file/getFileObject")
    Resource getFileObject(@RequestParam("sessionId") String sessionId);

    @PostMapping("/searchVector")
    List<Document> searchVector(@RequestParam("sessionId") String sessionId);
}
