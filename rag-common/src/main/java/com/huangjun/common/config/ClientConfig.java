package com.huangjun.common.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean("ragClient")
    public ChatClient ragClient(
            OpenAiChatModel  openAiChatModel,
            ChatMemory chatMemory,
            @Qualifier("redisVectorStore") VectorStore vectorStore
    ) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("""
                    你是一个专业的智能简历助手。
                    请严格根据提供的上下文资料（Context）来回答用户的问题。
                    如果上下文中没有包含明确的答案，请直接回答“根据提供的资料，我无法回答该问题”，绝对不要自行编造或发散。
                    """)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(2)
                                                .similarityThreshold(0.7)
                                                .build()
                                )
                                .build()
                )
                .build();
    }


    @Bean("fileClient")
    public ChatClient fileClient(
            OpenAiChatModel  openAiChatModel,
            @Qualifier("redisVectorStore") VectorStore vectorStore
    ) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("""
                你是一个专业的简历数据清洗助手。请从以下排版混乱的 OCR 文本中提取简历信息。
                
                【提取规则】
                1. 必须完全忠于原文：只提取文本中真实存在的大标题和对应内容。
                2. 绝不允许脑补：如果文本中没有“姓名”、“电话”等信息，不要自行添加，也不要输出“未知”。
                3. 内容拆分：同一个大标题下，如果有多个不同的经历或项目，请将它们拆分开，作为独立的字符串放入 contents 列表中。尽量保留时间、技术栈、职责等核心信息，去掉无意义的换行。
                
                OCR文本：
                {ocrText}
                
                【输出格式要求】
                {format}
                """)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        QuestionAnswerAdvisor
                                .builder(vectorStore)
                                .build()
                )
                .build();
    }

}
