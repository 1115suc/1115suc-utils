package com.course.langchain.entity.dao;

import lombok.Data;

@Data
public class FileEmbeddingDao {
    private String fileId;
    private String fileName;
    private String filePath;
    private String fileUrl;
}
