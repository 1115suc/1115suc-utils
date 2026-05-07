package com.course.langchain.assistant;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
interface RagAssistant {
    String chat(@UserMessage String message);
}
