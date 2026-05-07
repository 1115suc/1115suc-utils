package com.course.langchain.config;

import com.course.langchain.properties.EmbeddingModelProperties;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(EmbeddingModelProperties.class)
@ConditionalOnBean(EmbeddingModelProperties.class)
public class EmbeddingModelConfig {

    private final EmbeddingModelProperties embeddingModelProperties;

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .apiKey(embeddingModelProperties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .dimensions(embeddingModelProperties.getDimensions())
                .timeout(Duration.ofSeconds(embeddingModelProperties.getTimeout()))
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .logRequests(embeddingModelProperties.getLogRequest())
                .logResponses(embeddingModelProperties.getLogResponse())
                .build();
    }
}
