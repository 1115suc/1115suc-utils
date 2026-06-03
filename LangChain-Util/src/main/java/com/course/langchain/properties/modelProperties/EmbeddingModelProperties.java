package com.course.langchain.properties.modelProperties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "model.embedding")
@ConditionalOnProperty(prefix = "model.embedding", value = "baseUrl")
public class EmbeddingModelProperties {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Integer dimensions;
    private Integer timeout = 30;
    private Integer maxRetries = 2;
    private Boolean logRequest = false;
    private Boolean logResponse = false;
}
