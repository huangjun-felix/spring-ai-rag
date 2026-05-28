package com.huangjun.common.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(indexName = "business_record")
public class DocumentData {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private List<String> contents;

    @Field(type = FieldType.Keyword)
    private String sessionId;

    @Field(type = FieldType.Keyword)
    private String parentId;

    @Field(type = FieldType.Keyword)
    private String docType;

    @Field(type = FieldType.Text)
    private String fullText;

    @Field(type = FieldType.Date)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "GMT+8")
    private Date createTime;

    public static DocumentData of(String title, List<String> contents, String sessionId, Date createTime) {
        DocumentData data = new DocumentData();
        data.setId(UUID.randomUUID().toString());
        data.setTitle(title);
        data.setContents(contents);
        data.setSessionId(sessionId);
        data.setCreateTime(createTime);
        return data;
    }

    public static DocumentData parent(String title, List<String> contents, String fullText, String sessionId, Date createTime) {
        DocumentData data = new DocumentData();
        data.setId(UUID.randomUUID().toString());
        data.setTitle(title);
        data.setContents(contents);
        data.setFullText(fullText);
        data.setSessionId(sessionId);
        data.setCreateTime(createTime);
        data.setDocType("parent");
        return data;
    }

    public static DocumentData child(String title, String chunkText, String parentId, String sessionId, Date createTime) {
        DocumentData data = new DocumentData();
        data.setId(UUID.randomUUID().toString());
        data.setTitle(title);
        data.setContents(List.of(chunkText));
        data.setSessionId(sessionId);
        data.setCreateTime(createTime);
        data.setParentId(parentId);
        data.setDocType("child");
        return data;
    }

    public static DocumentData entity(String entityName, String entityType, String relatedParentId,
                                      String relatedSection, String sessionId, Date createTime) {
        DocumentData data = new DocumentData();
        data.setId(UUID.randomUUID().toString());
        data.setTitle(entityName);
        data.setContents(List.of(entityType + ": " + entityName));
        data.setSessionId(sessionId);
        data.setCreateTime(createTime);
        data.setParentId(relatedParentId);
        data.setDocType("entity");
        data.setFullText(entityType);
        return data;
    }
}
