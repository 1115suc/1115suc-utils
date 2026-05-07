package com.course.langchain.config;

import com.course.langchain.handler.MongoMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ModelMemoryConfig {

    private final MongoMemoryStore mongoMemoryStore;

    //构建ChatMemoryProvider对象
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(15)
                        .chatMemoryStore(mongoMemoryStore)
                        .build();
            }
        };
    }
}
