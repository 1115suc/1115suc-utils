package com.course.langchain.config.modelConfig;

import com.course.langchain.properties.modelProperties.ChatModelProperties;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ChatModelProperties.class)
@ConditionalOnBean(ChatModelProperties.class)
public class ChatModelConfig {

    private final ChatModelProperties chatModelProperties;

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(chatModelProperties.getBaseUrl())
                .apiKey(chatModelProperties.getApiKey())
                .modelName(chatModelProperties.getModelName())
                .topP(chatModelProperties.getTopP())
                .temperature(chatModelProperties.getTemperature())
                .maxTokens(chatModelProperties.getMaxTokens())
                .frequencyPenalty(chatModelProperties.getFrequencyPenalty())
                .timeout(Duration.ofSeconds(chatModelProperties.getTimeout()))
                .logRequests(chatModelProperties.getLogRequest())
                .logResponses(chatModelProperties.getLogResponse())
                .build();
    }
}
