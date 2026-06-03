package com.course.langchain.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface RagAssistant {
    String chat(@UserMessage String message);

    @SystemMessage("请根据提示词扮演好专业的角色。{{promote}}")
    String chatWithSystem(@UserMessage String message, @V("promote") String promote);
}
