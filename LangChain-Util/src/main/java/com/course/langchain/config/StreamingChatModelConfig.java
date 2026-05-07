package com.course.langchain.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "model.chat.streaming")
@ConditionalOnProperty(name = "model.chat.streaming")
public class StreamingChatModelConfig {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Double temperature = 0.6;
    private Integer maxTokens = 2048;
    private Double frequencyPenalty = 0.0;
    private Integer timeout = 60;
    private Boolean logRequest = false;
    private Boolean logResponse = false;

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .frequencyPenalty(frequencyPenalty)
                .timeout(Duration.ofSeconds(timeout))
                .logRequests(logRequest)
                .logResponses(logResponse)
                .build();
    }
}
