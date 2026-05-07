package com.course.langchain.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConditionalOnProperty(prefix = "model.embedding")
@ConfigurationProperties(prefix = "model.embedding")
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
