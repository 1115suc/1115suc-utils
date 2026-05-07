package com.course.langchain.assistant;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
interface StreamingChatAssistant {
    String chat(@UserMessage String message);

    String imageChat(@UserMessage String message, @UserMessage ImageContent imageList);

    String imageChat(@UserMessage String message, @UserMessage List<ImageContent> imageList);

    String audioChat(@UserMessage String message, @UserMessage AudioContent audioList);

    String audioChat(@UserMessage String message, @UserMessage List<AudioContent> audioList);

    String videoChat(@UserMessage String message, @UserMessage VideoContent videoList);

    String videoChat(@UserMessage String message, @UserMessage List<VideoContent> videoList);

    String pdfChat(@UserMessage String message, @UserMessage PdfFileContent pdfFileContent);

    String pdfChat(@UserMessage String message, @UserMessage List<PdfFileContent> pdfFileContent);
}
