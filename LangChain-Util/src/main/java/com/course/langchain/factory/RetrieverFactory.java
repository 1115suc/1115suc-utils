package com.course.langchain.factory;

import com.course.langchain.config.ChromaConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.logical.And;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Component
@ConditionalOnBean(ChromaConfig.class)
@RequiredArgsConstructor
public class RetrieverFactory {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 功能一：fileIds（数组）+ sessionId，均可为空
     */
    public ContentRetriever buildRetriever(List<Long> fileIds, String sessionId) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .dynamicFilter(query -> buildCombinedFilter(fileIds, sessionId))
                .build();
    }

    /**
     * 功能二：仅按 fileIds（数组）过滤，可为空
     */
    public ContentRetriever buildFileRetriever(List<Long> fileIds) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .dynamicFilter(query -> buildFileFilter(fileIds))
                .build();
    }

    /**
     * 功能三：仅按 sessionId 过滤，可为空
     * - sessionId 有值 → 按 sessionId 过滤
     * - sessionId 为空 → 不过滤（全量检索）
     */
    public ContentRetriever buildSessionOnlyRetriever(String sessionId) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .dynamicFilter(query -> (sessionId != null && !sessionId.isBlank())
                        ? metadataKey("sessionId").isEqualTo(sessionId)
                        : null)
                .build();
    }

    // -------------------------------------------------------------------------

    private Filter buildCombinedFilter(List<Long> fileIds, String sessionId) {
        Filter fileFilter    = buildFileFilter(fileIds);
        Filter sessionFilter = (sessionId != null && !sessionId.isBlank())
                ? metadataKey("sessionId").isEqualTo(sessionId)
                : null;

        if (fileFilter != null && sessionFilter != null) return new And(fileFilter, sessionFilter);
        if (sessionFilter != null) return sessionFilter;
        if (fileFilter != null)    return fileFilter;
        return null;
    }

    /**
     * fileIds 为空集合或 null → 返回 null（不过滤）
     * fileIds 只有一个元素  → isEqualTo（性能优于 isIn）
     * fileIds 有多个元素   → isIn
     */
    private Filter buildFileFilter(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return null;
        if (fileIds.size() == 1) return metadataKey("fileId").isEqualTo(fileIds.get(0));
        return metadataKey("fileId").isIn(fileIds);
    }
}