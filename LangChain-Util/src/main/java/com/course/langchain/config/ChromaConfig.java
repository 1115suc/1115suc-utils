package com.course.langchain.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty(name = "model.vector.chroma", havingValue = "baseUrl")
@ConfigurationProperties(prefix = "model.vector.chroma")
public class ChromaConfig {

    private String baseUrl;
    private String collectionName;
    private int timeout = 30;

    @Bean
    public EmbeddingStore<TextSegment> chromaEmbeddingStore() {
        log.info("正在初始化 Chroma 向量存储 - URL: {}, Collection: {}, Timeout: {}s",
                baseUrl, collectionName, timeout);
        try {
            ChromaEmbeddingStore store = ChromaEmbeddingStore.builder()
                    .baseUrl(baseUrl)
                    .collectionName(collectionName != null && !collectionName.trim().isEmpty() ? collectionName : "default")
                    .timeout(java.time.Duration.ofSeconds(timeout))
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