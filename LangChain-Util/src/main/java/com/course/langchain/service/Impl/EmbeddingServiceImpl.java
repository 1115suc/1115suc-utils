package com.course.langchain.service.Impl;

import cn.hutool.core.collection.CollUtil;
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
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;


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
            log.error(msg, e);
            throw new RuntimeException(msg, e);
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
            String msg = String.format("[RAG] 目的地址 %s 文件加载失败", file.getFileUrl());
            log.error(msg, e);
            throw new RuntimeException(msg, e);
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
        // 入库前校验：同一 fileId 已存在则直接拒绝，避免重复入库
        assertFileIdNotExists(file.getFileId());

        if (StrUtil.isBlank(document.text())) {
            log.warn("[RAG] 文件 {} 内容为空，跳过向量化", file.getFileName());
            return CommonConstants.error;
        }

        // 文档分段
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
        List<TextSegment> segments = splitter.split(document);

        if (CollUtil.isEmpty(segments)) {
            log.warn("[RAG] 文件 {} 分段结果为空，跳过", file.getFileName());
            return CommonConstants.error;
        }

        // 构建带 metadata 的有效分段
        List<TextSegment> finalSegments = new ArrayList<>(segments.size());
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            if (StrUtil.isBlank(segment.text())) {
                log.debug("[RAG] 第 {} 段为空，跳过", i);
                continue;
            }

            //  Chroma metadata：值支持 String / int / float / boolean
            Metadata metadata = new Metadata();
            metadata.put("fileId", file.getFileId());
            metadata.put("index", i);

            finalSegments.add(TextSegment.from(segment.text(), metadata));
        }

        if (finalSegments.isEmpty()) {
            log.warn("[RAG] 文件 {} 没有有效分段可存储", file.getFileName());
            return CommonConstants.error;
        }

        // 批量生成 embedding（一次远程请求，避免逐段调用）
        List<Embedding> embeddings = embeddingModel.embedAll(finalSegments).content();
        log.info("[RAG] 模型输出向量维度: {}", embeddings.get(0).vector().length);

        // 批量存入 Chroma
        embeddingStore.addAll(embeddings, finalSegments);

        log.info("[RAG] 文件 {} 向量化完成, 有效chunks={}",
                file.getFileName(), finalSegments.size());
        return CommonConstants.success;
    }

    /**
     * 校验 fileId 是否已入库，存在则抛出异常。
     * 通过 metadata 过滤 + minScore=0 检索任意一条匹配记录来判断存在性，
     * queryEmbedding 仅用于满足检索接口要求，不影响过滤结果。
     */
    private void assertFileIdNotExists(String fileId) {
        Embedding probe = embeddingModel.embed(fileId).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(probe)
                .filter(metadataKey("fileId").isEqualTo(fileId))
                .maxResults(1)
                .minScore(0.0)
                .build();

        if (CollUtil.isNotEmpty(embeddingStore.search(request).matches())) {
            String msg = String.format("[RAG] fileId %s 已存在，禁止重复入库", fileId);
            log.warn(msg);
            throw new RuntimeException(msg);
        }
    }
}
