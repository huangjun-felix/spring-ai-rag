package com.huangjun.file.service;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public interface PdfVectorService {

    void savePdfVector(Resource resource, String sessionId) throws IOException;
    List<Document> searchPdfVector(Resource resource, String sessionId);

}
