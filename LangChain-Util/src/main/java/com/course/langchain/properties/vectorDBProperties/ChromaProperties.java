package com.course.langchain.properties.vectorDBProperties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "model.vector.chroma")
@ConditionalOnProperty(prefix = "model.vector.chroma", value = "baseUrl")
public class ChromaProperties {
    private String baseUrl;
    private String collectionName;
    private int timeout = 30;
}
