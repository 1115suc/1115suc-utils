package com.course.langchain.util;

import cn.hutool.core.util.ObjectUtil;
import com.course.langchain.entity.dao.UserAIDao;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuildModelUtil {

    private static final Double DEFAULT_TEMPERATURE = 0.7;
    private static final Double DEFAULT_TOP_P = 1.0;
    private static final Double DEFAULT_FREQUENCYPENALTY = 0.0;
    private static final Integer DEFAULT_MAX_TOKENS = 2048;
    private static final Integer DEFAULT_TIMEOUT = 30;

    //  构建 StreamingChatLanguageModel（自定义模型 > 系统默认）
    public OpenAiChatModel buildChatModel(UserAIDao userAI) {
        String baseUrl = userAI.getBaseUrl();
        String modelName = userAI.getModelName();
        String apiKey = userAI.getApiKey();

        Double temperature = ObjectUtil.isNotNull(userAI.getTemperature())
                ? userAI.getTemperature()
                : DEFAULT_TEMPERATURE;
        Double topP = ObjectUtil.isNotNull(userAI.getTopP())
                ? userAI.getTopP()
                : DEFAULT_TOP_P;
        Double frequencyPenalty = ObjectUtil.isNotNull(userAI.getFrequencyPenalty())
                ? userAI.getFrequencyPenalty()
                : DEFAULT_FREQUENCYPENALTY;
        Integer maxTokens = ObjectUtil.isNotNull(userAI.getMaxTokens())
                ? userAI.getMaxTokens()
                : DEFAULT_MAX_TOKENS;
        Integer timeout = ObjectUtil.isNotNull(userAI.getTimeout())
                ? userAI.getTimeout()
                : DEFAULT_TIMEOUT;

        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .apiKey(apiKey)
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .frequencyPenalty(frequencyPenalty)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }

    public OpenAiStreamingChatModel buildStreamingChatModel(UserAIDao userAI) {
        String baseUrl = userAI.getBaseUrl();
        String modelName = userAI.getModelName();
        String apiKey = userAI.getApiKey();

        Double temperature = ObjectUtil.isNotNull(userAI.getTemperature())
                ? userAI.getTemperature()
                : DEFAULT_TEMPERATURE;
        Double topP = ObjectUtil.isNotNull(userAI.getTopP())
                ? userAI.getTopP()
                : DEFAULT_TOP_P;
        Double frequencyPenalty = ObjectUtil.isNotNull(userAI.getFrequencyPenalty())
                ? userAI.getFrequencyPenalty()
                : DEFAULT_FREQUENCYPENALTY;
        Integer maxTokens = ObjectUtil.isNotNull(userAI.getMaxTokens())
                ? userAI.getMaxTokens()
                : DEFAULT_MAX_TOKENS;
        Integer timeout = ObjectUtil.isNotNull(userAI.getTimeout())
                ? userAI.getTimeout()
                : DEFAULT_TIMEOUT;

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .apiKey(apiKey)
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .frequencyPenalty(frequencyPenalty)
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }
}
