package com.course.langchain.properties.modelProperties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "model.chat")
@ConditionalOnProperty(prefix = "model.chat", value = "baseUrl")
public class ChatModelProperties {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Double topP = 0.6;
    private Double temperature = 0.6;
    private Integer maxTokens = 2048;
    private Double frequencyPenalty = 0.0;
    private Integer timeout = 30;
    private Boolean logRequest = false;
    private Boolean logResponse = false;
}
