package com.course.langchain.entity.dao;

import lombok.Data;

@Data
public class UserAIDao {
    private String baseUrl;
    private String modelName;
    private String apiKey;
    private Double topP;
    private Double temperature;
    private Double frequencyPenalty;
    private Integer maxTokens;
    private Integer timeout;
    private Boolean logRequest = false;
    private Boolean logResponse = false;
}
