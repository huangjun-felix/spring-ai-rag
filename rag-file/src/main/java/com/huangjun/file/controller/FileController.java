package com.huangjun.file.controller;

import com.huangjun.common.domain.FileInfo;
import com.huangjun.file.service.FileDataService;
import com.huangjun.file.service.FileService;
import com.huangjun.file.service.PdfVectorService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {
    private FileService fileService;
    private FileDataService fileDataService;
    private PdfVectorService  pdfVectorService;
    @Autowired
    public void setServices(
            FileService fileService,
            FileDataService fileDataService,
            PdfVectorService pdfVectorService
    ) {
        this.fileService = fileService;
        this.fileDataService = fileDataService;
        this.pdfVectorService = pdfVectorService;
    }


    @PostMapping("/upload/{sessionId}")
    public ResponseEntity<String> fileUpload(@PathVariable("sessionId") String sessionId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
//            String fileName = fileService.fileUpload(file);
//            fileDataService.save(fileName, sessionId);
            pdfVectorService.savePdfVector(file.getResource(),sessionId);
            return ResponseEntity.ok("fileName");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/url/{sessionId}")
    public String getFileUrl(@PathVariable("sessionId") String sessionId) {
        FileInfo fileInfo = fileDataService.getBySessionId(sessionId);
        try {
            return fileService.getFileUrl(fileInfo.getFileName());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable("fileName") String fileName) {
        fileService.deleteFile(fileName);
        fileDataService.deleteByFileName(fileName);
        return ResponseEntity.ok(fileName);
    }

    @PostMapping("/getFileObject")
    public Resource getFileObject(@RequestParam("sessionId") String sessionId) {
        FileInfo fileInfo = fileDataService.getBySessionId(sessionId);
        if (fileInfo == null) {
            return null;
        }
        InputStream stream = fileService.getFileObject(fileInfo.getFileName());
        return new InputStreamResource(stream);
    }

    @PostMapping("/searchVector")
    public List<Document> searchVector(@RequestParam("sessionId") String sessionId) {
        FileInfo fileInfo = fileDataService.getBySessionId(sessionId);
        if (fileInfo == null) {
            return null;
        }
        InputStream file = fileService.getFileObject(fileInfo.getFileName());
        return pdfVectorService.searchPdfVector(new InputStreamResource(file), sessionId);
    }

}
