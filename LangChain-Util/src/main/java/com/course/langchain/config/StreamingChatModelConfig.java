package com.course.langchain.config;

import com.course.langchain.properties.StreamingChatModelProperties;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(StreamingChatModelProperties.class)
@EnableConfigurationProperties(StreamingChatModelProperties.class)
public class StreamingChatModelConfig {

    private final StreamingChatModelProperties streamingChatModelProperties;

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(streamingChatModelProperties.getBaseUrl())
                .apiKey(streamingChatModelProperties.getApiKey())
                .modelName(streamingChatModelProperties.getModelName())
                .temperature(streamingChatModelProperties.getTemperature())
                .maxTokens(streamingChatModelProperties.getMaxTokens())
                .frequencyPenalty(streamingChatModelProperties.getFrequencyPenalty())
                .timeout(Duration.ofSeconds(streamingChatModelProperties.getTimeout()))
                .logRequests(streamingChatModelProperties.getLogRequest())
                .logResponses(streamingChatModelProperties.getLogResponse())
                .build();
    }
}
