package com.course.langchain.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConditionalOnProperty(prefix = "model.vector.chroma")
@ConfigurationProperties(prefix = "model.vector.chroma")
public class ChromaProperties {
    private String baseUrl;
    private String collectionName;
    private int timeout = 30;
}
