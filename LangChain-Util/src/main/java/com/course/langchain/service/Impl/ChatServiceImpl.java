package com.course.langchain.service.Impl;

import com.course.langchain.assistant.ChatAssistant;
import com.course.langchain.service.ChatService;
import dev.langchain4j.data.message.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatAssistant chatAssistant;

    @Override
    public String chat(String message) {


        return "";
    }

    @Override
    public String chatWithOther(String message, List<Content> otherMessages) {
        return "";
    }
}
