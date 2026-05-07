package com.course.langchain.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "model.embedding")
@ConditionalOnProperty(name = "model.embedding")
public class EmbeddingModelConfig {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    // 嵌入向量的维度
    private Integer dimensions;
    private Integer timeout = 30;
    private Integer maxRetries = 2;
    private Boolean logRequest = false;
    private Boolean logResponse = false;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .dimensions(dimensions)
                .timeout(Duration.ofSeconds(timeout))
                .maxRetries(maxRetries)
                .logRequests(logRequest)
                .logResponses(logResponse)
                .build();
    }
}
