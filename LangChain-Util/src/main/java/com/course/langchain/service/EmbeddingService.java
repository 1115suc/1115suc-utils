package com.course.langchain.service;

import com.course.langchain.entity.dao.FileEmbeddingDao;

public interface EmbeddingService {
    // 通过文件路径解析文件
    Boolean ingestDocumentsByFile(FileEmbeddingDao fileEmbeddingDao);
    // 通过文件url解析文件
    Boolean ingestDocumentsByFileUrl(FileEmbeddingDao fileEmbeddingDao);
}
