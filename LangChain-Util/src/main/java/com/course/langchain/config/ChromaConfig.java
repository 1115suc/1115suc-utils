package com.course.langchain.config;

import com.course.langchain.properties.ChromaProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(ChromaProperties.class)
@EnableConfigurationProperties(ChromaProperties.class)
public class ChromaConfig {

    private final ChromaProperties chromaProperties;

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        log.info("正在初始化 Chroma 向量存储 - URL: {}, Collection: {}, Timeout: {}s",
                chromaProperties.getBaseUrl(), chromaProperties.getCollectionName(), chromaProperties.getTimeout());
        try {
            ChromaEmbeddingStore store = ChromaEmbeddingStore.builder()
                    .baseUrl(chromaProperties.getBaseUrl())
                    .collectionName(chromaProperties.getCollectionName() != null && !chromaProperties.getCollectionName().trim().isEmpty() ? chromaProperties.getCollectionName() : "default")
                    .timeout(java.time.Duration.ofSeconds(chromaProperties.getTimeout()))
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            log.info("Chroma 向量存储初始化成功");
            return store;
        } catch (Exception e) {
            throw new RuntimeException("初始化 Chroma 向量存储失败！详细错误：" + e.getMessage(), e);
        }
    }
}
