package com.course.langchain.properties.modelProperties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "model.chat.streaming")
@ConditionalOnProperty(prefix = "model.chat.streaming", value = "baseUrl")
public class StreamingChatModelProperties {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Double topP = 0.6;
    private Double temperature = 0.6;
    private Integer maxTokens = 2048;
    private Double frequencyPenalty = 0.0;
    private Integer timeout = 60;
    private Boolean logRequest = false;
    private Boolean logResponse = false;
}
