package com.huangjun.file.service.impl;

import com.huangjun.common.service.DocumentData;
import com.huangjun.file.constants.PromptConstants;
import com.huangjun.file.repository.PdfVectorRepository;
import com.huangjun.file.service.PdfVectorService;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class PdfVectorServiceImpl implements PdfVectorService {
    private static final Logger logger = LoggerFactory.getLogger(PdfVectorServiceImpl.class);
    private record ParentChildResult(List<DocumentData> parents, List<DocumentData> children, List<Document> vectorDocs) {}

    private PdfVectorRepository pdfVectorRepository;
    private ChatClient chatClient;
    private Executor taskScheduler;
    @Autowired
    public void init(PdfVectorRepository pdfVectorRepository,
                     ChatClient.Builder chatClient,
                     @Qualifier("taskAsync")  Executor taskScheduler
                     ) {
        this.pdfVectorRepository = pdfVectorRepository;
        this.chatClient = chatClient.build();
        this.taskScheduler = taskScheduler;
    }

    private boolean pdfIsEnableInVector(Resource resource, String sessionId){
        if (!resource.exists() || !resource.isReadable()) {
            return false;
        }
        if (!StringUtils.hasText(sessionId)){
            logger.error("sessionId不能为null");
            return false;
        }
        return true;
    }

    private boolean pdfIsEnableInVector(Resource resource){
        return pdfIsEnableInVector(resource,"null");
    }

    public record ResumeData(List<Section> sections) {}

    // 简历中的每一个大模块（比如：教育背景、项目经验、技能特长）
    public record Section(
            String title,         // 大标题，例如："项目经验"
            List<String> contents // 具体内容列表。例如：["oa学院系统 后端开发...", "巴南区执法办案中心..."]
    ) {}

    /**
     * 通用 PDF 解析入口
     *
     * @param inputStream PDF 文件的输入流
     * @return 提取出的纯文本（可能是原生文本，也可能是 OCR 识别出的文本）
     */
    public String parsePdf(InputStream inputStream) {
        try (PDDocument document = PDDocument.load(inputStream)) {
            // 第 1 步：尝试快速的原生文本提取
            String nativeText = extractNativeText(document);

            // 第 2 步：智能判定。如果提取为空，或者字数太少（比如整页只有几个字），说明是扫描件或字体损坏
            if (isTextValid(nativeText, document.getNumberOfPages())) {
                System.out.println("✅ 原生文本提取成功！");
                return nativeText;
            }

            // 第 3 步：降级处理。原生提取失败，启动 OCR 图像识别
            System.out.println("⚠️ 原生提取失效 (可能是扫描件或字体缺失)，正在自动降级为 OCR 识别...");
            return extractTextWithOCR(document);

        } catch (Exception e) {
            System.err.println("❌ PDF 解析发生严重错误: " + e.getMessage());
            return "";
        }
    }
    /**
     * 1. 标准原生文本提取 (极速)
     */
    private String extractNativeText(PDDocument document) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true); // 尽量保持版面原本的从左到右、从上到下的顺序
        return stripper.getText(document);
    }

    /**
     * 2. 校验原生文本是否有效
     */
    private boolean isTextValid(String text, int pageCount) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        // 简单策略：去除空白符后，平均每页字数少于 50 个字，极有可能是扫描件或乱码
        String trimmedText = text.replaceAll("\\s+", "");
        return trimmedText.length() > (pageCount * 50);
    }

    /**
     * 3. OCR 提取兜底 (将 PDF 每页渲染成图片，再进行光学识别)
     */
    private String extractTextWithOCR(PDDocument document) throws IOException {
        StringBuilder ocrResult = new StringBuilder();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        // 初始化 OCR 引擎
        ITesseract tesseract = new Tesseract();
        // 【注意】你需要提前下载中文语言包 chi_sim.traineddata 并放到该目录下
        tesseract.setDatapath("D:/tesseract/tessdata");
        tesseract.setLanguage("chi_sim"); // 设置为简体中文
        // 遍历 PDF 的每一页
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            // 将单页 PDF 渲染为 300 DPI 的高清图片 (DPI越高，OCR越准，但越慢)
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            try {
                // 对生成的图片进行文字识别
                String pageText = tesseract.doOCR(bim);
                ocrResult.append(pageText).append("\n");
            } catch (TesseractException e) {
                System.err.println("第 " + (page + 1) + " 页 OCR 识别失败: " + e.getMessage());
            }
        }
        return ocrResult.toString();
    }

    public void savePdfVector(Resource resource, String sessionId) throws IOException {
        if (!pdfIsEnableInVector(resource, sessionId)) return;
        String pdfText = parsePdf(resource.getInputStream());
        if (!StringUtils.hasText(pdfText)) { logger.error("PDF 文本提取结果为空"); return; }
        logger.info("PDF 文本提取完成，共 {} 个字符", pdfText.length());

        BeanOutputConverter<ResumeData> converter = new BeanOutputConverter<>(ResumeData.class);
        String systemPrompt = PromptConstants.PDF_CLEAR_PROMPT
                + "\n\n【重要】请严格按照以下 JSON 格式输出，不要包含任何 Markdown 标记：\n" + converter.getFormat();

        ParentChildResult result;
        if (pdfText.length() <= 8000) {
            result = processChunkWithParentChild(pdfText, systemPrompt, converter, sessionId);
        } else {
            result = processInParallelWithParentChild(pdfText, systemPrompt, converter, sessionId);
        }

        if (result.vectorDocs().isEmpty()) { logger.warn("向量数据为空"); return; }

        List<DocumentData> entities = extractEntities(result.parents(), sessionId);
        pdfVectorRepository.savePdfVectorWithGraph(result.vectorDocs(), result.parents(), result.children(), entities);
        logger.info("父子索引+GraphRAG完成: 父块={} 子块={} 实体={}", result.parents().size(), result.children().size(), entities.size());
    }

    // ==================== 父子索引 ====================
    private ParentChildResult buildParentChildResult(ResumeData data, String sessionId) {
        List<DocumentData> parents = new ArrayList<>();
        List<DocumentData> children = new ArrayList<>();
        List<Document> vectorDocs = new ArrayList<>();
        for (Section section : data.sections()) {
            String fullText = String.join("\n", section.contents());
            DocumentData parent = DocumentData.parent(section.title(), section.contents(), fullText, sessionId, new Date());
            parents.add(parent);
            vectorDocs.addAll(convertToVectorDocs(section, sessionId));
            for (String content : section.contents()) {
                children.add(DocumentData.child(section.title(), content, parent.getId(), sessionId, new Date()));
            }
        }
        return new ParentChildResult(parents, children, vectorDocs);
    }

    private ParentChildResult processChunkWithParentChild(String text, String systemPrompt,
                                                          BeanOutputConverter<ResumeData> converter, String sessionId) {
        try {
            String json = chatClient.prompt().system(systemPrompt).user(text).call().content();
            if (!StringUtils.hasText(json)) return emptyResult();
            ResumeData data = converter.convert(json);
            if (data.sections() == null || data.sections().isEmpty()) return emptyResult();
            return buildParentChildResult(data, sessionId);
        } catch (Exception e) { logger.error("AI 处理失败", e); return emptyResult(); }
    }

    private ParentChildResult processInParallelWithParentChild(String text, String systemPrompt,
                                                               BeanOutputConverter<ResumeData> converter, String sessionId) {
        TokenTextSplitter splitter = TokenTextSplitter.builder().withChunkSize(5000).withKeepSeparator(true).build();
        List<Document> chunks = splitter.split(Document.builder().text(text).build());
        List<CompletableFuture<ParentChildResult>> futures = chunks.stream()
                .filter(doc -> StringUtils.hasText(doc.getText()))
                .map(doc -> CompletableFuture.supplyAsync(
                        () -> processChunkWithParentChild(doc.getText(), systemPrompt, converter, sessionId), taskScheduler))
                .toList();
        List<ParentChildResult> results = futures.stream().map(CompletableFuture::join).toList();
        return new ParentChildResult(
                results.stream().flatMap(r -> r.parents().stream()).toList(),
                results.stream().flatMap(r -> r.children().stream()).toList(),
                results.stream().flatMap(r -> r.vectorDocs().stream()).toList());
    }

    private ParentChildResult emptyResult() {
        return new ParentChildResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    private List<Document> convertToVectorDocs(Section section, String sessionId) {
        List<Document> docs = new ArrayList<>();
        if (section.contents() == null) return docs;
        for (String content : section.contents()) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("session_id", sessionId); meta.put("section_title", section.title());
            docs.add(new Document("【" + section.title() + "】 " + content, meta));
        }
        return docs;
    }

    // ==================== 实体抽取 (GraphRAG) ====================
    private List<DocumentData> extractEntities(List<DocumentData> parents, String sessionId) {
        try {
            StringBuilder ctx = new StringBuilder();
            for (DocumentData p : parents) {
                ctx.append("【").append(p.getTitle()).append("】\n");
                if (p.getFullText() != null) ctx.append(p.getFullText()).append("\n\n");
            }
            String prompt = "从以下文本提取关键实体，仅返回JSON数组: [{\"name\":\"实体名\",\"type\":\"类型\"},...]\n类型: PERSON/SCHOOL/COMPANY/PROJECT/TECH/SKILL\n文本:\n" + ctx;
            String resp = chatClient.prompt().user(prompt).call().content();
            if (!StringUtils.hasText(resp)) return Collections.emptyList();
            return parseEntities(resp.replaceAll("```json?", "").replaceAll("```", "").trim(), parents, sessionId);
        } catch (Exception e) { logger.error("实体抽取失败", e); return Collections.emptyList(); }
    }

    private List<DocumentData> parseEntities(String json, List<DocumentData> parents, String sessionId) {
        List<DocumentData> entities = new ArrayList<>();
        for (String part : json.split("\\},\\s*\\{")) {
            String name = extractField(part, "name");
            String type = extractField(part, "type");
            if (name != null && type != null) {
                String pid = findRelatedParent(name, type, parents);
                String section = parents.stream().filter(p -> p.getId().equals(pid)).map(DocumentData::getTitle).findFirst().orElse("");
                entities.add(DocumentData.entity(name, type, pid, section, sessionId, new Date()));
            }
        }
        return entities;
    }

    private String extractField(String part, String field) {
        int idx = part.indexOf(field + "\":");
        if (idx < 0) idx = part.indexOf(field + ":");
        if (idx < 0) return null;
        int start = part.indexOf(':', idx) + 1;
        while (start < part.length() && (part.charAt(start) == ' ' || part.charAt(start) == '"')) start++;
        int end = start;
        while (end < part.length() && part.charAt(end) != ',' && part.charAt(end) != '\n' && part.charAt(end) != '"') end++;
        String v = part.substring(start, end).trim().replaceAll("\"", "");
        return v.isEmpty() ? null : v;
    }

    private String findRelatedParent(String name, String type, List<DocumentData> parents) {
        for (DocumentData p : parents) if (p.getTitle().contains(name)) return p.getId();
        String expected = switch (type) { case "SCHOOL" -> "教育背景"; case "TECH","SKILL" -> "专业技能"; case "PROJECT" -> "项目经验"; case "COMPANY" -> "工作经历"; default -> null; };
        if (expected != null) for (DocumentData p : parents) if (p.getTitle().contains(expected)) return p.getId();
        return parents.isEmpty() ? null : parents.get(0).getId();
    }

    // ==================== 旧方法保留 ====================
    private List<Document> convertToVectorDocuments(ResumeData data, String sessionId) {
        List<Document> docs = new ArrayList<>();
        if (data.sections() == null) return docs;
        for (Section section : data.sections())
            for (String content : section.contents()) {
                Map<String, Object> meta = new HashMap<>();
                meta.put("session_id", sessionId); meta.put("section_title", section.title());
                docs.add(new Document("【" + section.title() + "】 " + content, meta));
            }
        return docs;
    }

    public List<Document> searchPdfVector(Resource resource, String sessionId){
        if (!pdfIsEnableInVector(resource,sessionId)){
            return null;
        }
        return pdfVectorRepository.searchPdfVector(resource,sessionId);
    }

}
