package com.course.langchain.service.Impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.course.langchain.constants.CommonConstants;
import com.course.langchain.entity.dao.FileEmbeddingDao;
import com.course.langchain.service.EmbeddingService;
import com.course.langchain.util.DocumentParserUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final DocumentParserUtil documentParserUtil;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public Boolean ingestDocumentsByFile(FileEmbeddingDao file) {
        if (!StrUtil.isAllNotBlank(file.getFileId(), file.getFileName(), file.getFilePath())) {
            throw new RuntimeException("[RAG] 参数校验错误");
        }

        // 对文档进行解析
        DocumentParser documentParser = documentParserUtil.resolveParser(file.getFileName());
        Document document;
        try {
            document = FileSystemDocumentLoader.loadDocument(file.getFilePath(), documentParser);
        } catch (Exception e) {
            String msg = String.format("[RAG] 目的路径 %s 文件加载失败", file.getFilePath());
            throw new RuntimeException(msg);
        }

        if (!ingestDocument(document, file)) {
            return CommonConstants.error;
        } else {
            log.info("[RAG] 文件 {} 向量化完成，session={}", file.getFileName(), file.getFileId());
            return CommonConstants.success;
        }
    }

    @Override
    public Boolean ingestDocumentsByFileUrl(FileEmbeddingDao file) {
        if (!StrUtil.isAllNotBlank(file.getFileId(), file.getFileName(), file.getFileUrl())) {
            throw new RuntimeException("[RAG] 参数校验错误");
        }

        // 对文档进行解析
        DocumentParser documentParser = documentParserUtil.resolveParser(file.getFileName());
        Document document;
        try {
            document = UrlDocumentLoader.load(file.getFileUrl(), documentParser);
        } catch (Exception e) {
            String msg = String.format("[RAG] 目的路径 %s 文件加载失败", file.getFilePath());
            throw new RuntimeException(msg);
        }

        if (!ingestDocument(document, file)) {
            return CommonConstants.error;
        } else {
            log.info("[RAG] 文件 {} 向量化完成，session={}", file.getFileName(), file.getFileId());
            return CommonConstants.success;
        }
    }

    // ===========================================================================================
    // 私有方法
    // ===========================================================================================

    private Boolean ingestDocument(Document document, FileEmbeddingDao file) {
        if (StrUtil.isBlank(document.text())) {
            String msg = String.format("[RAG] 文件 %s 内容为空，跳过向量化", file.getFileName());
            return CommonConstants.error;
        }

        // 文档分段
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
        List<TextSegment> segments = splitter.split(document);

        if (ArrayUtil.isEmpty(segments)) {
            log.warn("[RAG] 文件 {} 分段结果为空，跳过", file.getFileName());
            return CommonConstants.error;
        }

        // 构建带 metadata 的分段 + 生成 embedding
        List<TextSegment> finalSegments = new ArrayList<>(segments.size());
        List<Embedding> embeddings = new ArrayList<>(segments.size());

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            if (StrUtil.isBlank(segment.text())) {
                log.debug("[RAG] 第 {} 段为空，跳过", i);
                continue;
            }

            //  Chroma metadata：值支持 String / int / float / boolean
            Metadata metadata = new Metadata();
            metadata.put("fileId", file.getFileId());
            metadata.put("index", String.valueOf(i));

            TextSegment newSegment = TextSegment.from(segment.text(), metadata);
            Embedding embedding = embeddingModel.embed(newSegment).content();

            if (i == 0) {
                log.info("[RAG] 模型输出向量维度: {}", embedding.vector().length);
            }
            if (embedding.vector().length == 0) {
                log.error("[RAG] 第 {} 段生成的向量维度为 0，跳过", i);
                continue;
            }

            finalSegments.add(newSegment);
            embeddings.add(embedding);
        }

        if (finalSegments.isEmpty()) {
            log.warn("[RAG] 文件 {} 没有有效分段可存储", file.getFileName());
            return CommonConstants.error;
        }

        // 批量存入 Chroma
        embeddingStore.addAll(embeddings, finalSegments);

        log.info("[RAG] 文件 {} 向量化完成, 有效chunks={}",
                file.getFileName(), finalSegments.size());
        return CommonConstants.success;
    }
}
