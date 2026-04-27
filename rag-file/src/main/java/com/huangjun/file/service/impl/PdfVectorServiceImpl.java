package com.huangjun.file.service.impl;

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
        if (!pdfIsEnableInVector(resource,sessionId)){
            return;
        }
        String string = parsePdf(resource.getInputStream());
//        System.out.println("String:"+string);
//                PagePdfDocumentReader reader = new PagePdfDocumentReader(
//                resource,
//                PdfDocumentReaderConfig.builder()
//                        .withPagesPerDocument(1)
//                        .build()
//        );
//        List<Document> documents = reader.read();
//        documents.forEach(document -> document.getMetadata().put("session_id",sessionId));
//        用这个将pdf的内容分成一页一页
//        PagePdfDocumentReader reader = new PagePdfDocumentReader(
//                resource,
//                PdfDocumentReaderConfig
//                        .builder()
//                        .withPagesPerDocument(1)
//                        .build()
//        );
//        List<Document> documents = reader.get();
        Document documents = Document.builder()
                .text(string)
                .metadata("session_id", sessionId)
                .build();
        //设置想要ai输出的json格式
        BeanOutputConverter<ResumeData> converter = new BeanOutputConverter<>(ResumeData.class);
        String format = converter.getFormat();
        //加到prompt提示词中
        String systemPrompt = PromptConstants.PDF_CLEAR_PROMPT + "\n\n【重要】请严格按照以下 JSON 格式输出，不要包含任何 Markdown 标记：\n" + format;
        //构建token拆分器
        TokenTextSplitter splitter = TokenTextSplitter
                .builder()
                .withChunkSize(2000)
                .withKeepSeparator(true)
                .build();
        //将pdf拆分为800token，
        List<Document> splitterDocuments = splitter.split(documents);

        List<CompletableFuture<List<Document>>> futureList = splitterDocuments
                .stream().filter(doc->StringUtils.hasText(doc.getText()))
                .map(document -> CompletableFuture.supplyAsync(()->{
                    String text = document.getText();
                    try {
                        String jsonResponse = chatClient.prompt()
                                .system(systemPrompt)
                                .user(text)
                                .call()
                                .content();
                        if (!StringUtils.hasText(jsonResponse)) return Collections.<Document>emptyList();
                        ResumeData resumeData = converter.convert(jsonResponse);
                        return convertToVectorDocuments(resumeData,sessionId);
                    }catch (Exception e){
                        System.out.println("处理 PDF 页码 " + document.getMetadata().get("page_number") + " 时出错: " + e.getMessage());
                        return Collections.<Document>emptyList();
                    }
                },taskScheduler))
                .toList();

        List<Document> allVectorDocuments = futureList.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();
        if (allVectorDocuments.isEmpty())return;



//        for (Document document : splitterDocuments) {
//            String text = document.getText();
//            if (!StringUtils.hasText(text)) continue;
//            try {
//                CompletableFuture<String> response = getStringClient(systemPrompt,text);
//                String jsonResponse = response.get();
//                if (!StringUtils.hasText(jsonResponse)) continue;
//                ResumeData resumeData = converter.convert(jsonResponse);
//                List<Document> documentList = convertToVectorDocuments(resumeData, sessionId);
//                allVectorDocuments.addAll(documentList);
//            }catch (Exception e){
//                System.out.println("处理 PDF 页码 " + document.getMetadata().get("page_number") + " 时出错: " + e.getMessage());
//            }
//        }
//        System.out.println("allVectorDocuments:"+ allVectorDocuments);
//        String pdfText = chatClient.prompt(systemPrompt)
//                .user()
//                .call()
//                .content();
//        System.out.println("pdfText: "+pdfText);
//        if (!StringUtils.hasText(pdfText)){
//            return;
//        }
//        System.out.println("pdfText: "+pdfText);
//        List<Document> documents = processAndSaveResume(pdfText, sessionId);
        pdfVectorRepository.savePdfVector(allVectorDocuments);
    }

    private List<Document> convertToVectorDocuments(ResumeData data, String sessionId) {
        List<Document> docs = new ArrayList<>();
        if (data.sections() == null) return docs;

        for (Section section : data.sections()) {
            for (String content : section.contents()) {
                // 组装文本：【模块名】具体内容
                String text = "【" + section.title() + "】 " + content;
                // 构建带元数据的 Document，方便检索
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("session_id", sessionId);
                metadata.put("section_title", section.title());

                docs.add(new Document(text, metadata));
            }
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
