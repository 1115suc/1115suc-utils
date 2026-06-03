package com.course.langchain.assistant;

import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

@AiService
public interface ChatAssistant {
    String chat(@UserMessage String message);

    String imageChat(@UserMessage String message, @UserMessage ImageContent imageList);

    String imageChat(@UserMessage String message, @UserMessage List<ImageContent> imageList);

    String audioChat(@UserMessage String message, @UserMessage AudioContent audioList);

    String audioChat(@UserMessage String message, @UserMessage List<AudioContent> audioList);

    String videoChat(@UserMessage String message, @UserMessage VideoContent videoList);

    String videoChat(@UserMessage String message, @UserMessage List<VideoContent> videoList);

    String pdfChat(@UserMessage String message, @UserMessage PdfFileContent pdfFileContent);

    String pdfChat(@UserMessage String message, @UserMessage List<PdfFileContent> pdfFileContent);

    @SystemMessage("你是我的助手，熟悉Java后端开发以及Vue3前端开发，根据提示词回答我的问题。{{promote}}")
    String chatWithSystem(@UserMessage String message, @V("promote") String promote);

    // 图片识别
    @SystemMessage("你是图像分析专家，仔细观察用户上传的图片，根据提示词进行详细分析并给出专业回答。{{promote}}")
    String imageChatWithSystem(@UserMessage String message, @UserMessage ImageContent imageContent, @V("promote") String promote);

    @SystemMessage("你是图像分析专家，仔细观察用户上传的所有图片，根据提示词逐一进行详细分析并给出专业回答。{{promote}}")
    String imageChatWithSystem(@UserMessage String message, @UserMessage List<ImageContent> imageContentList, @V("promote") String promote);

    // 音频识别
    @SystemMessage("你是音频分析专家，仔细聆听用户上传的音频文件，根据提示词准确转写和分析音频内容。{{promote}}")
    String audioChatWithSystem(@UserMessage String message, @UserMessage AudioContent audioContent, @V("promote") String promote);

    @SystemMessage("你是音频分析专家，仔细聆听用户上传的所有音频文件，根据提示词准确转写和分析音频内容。{{promote}}")
    String audioChatWithSystem(@UserMessage String message, @UserMessage List<AudioContent> audioContentList, @V("promote") String promote);

    // 视频识别
    @SystemMessage("你是视频内容分析专家，仔细观察用户上传的视频内容，根据提示词提取关键信息并给出专业分析。{{promote}}")
    String videoChatWithSystem(@UserMessage String message, @UserMessage VideoContent videoContent, @V("promote") String promote);

    @SystemMessage("你是视频内容分析专家，仔细观察用户上传的所有视频内容，根据提示词提取关键信息并给出专业分析。{{promote}}")
    String videoChatWithSystem(@UserMessage String message, @UserMessage List<VideoContent> videoContentList, @V("promote") String promote);

    // PDF文件识别
    @SystemMessage("你是文档分析专家，仔细阅读用户上传的PDF文件，根据提示词提取文档要点并给出专业回答。{{promote}}")
    String pdfChatWithSystem(@UserMessage String message, @UserMessage PdfFileContent pdfFileContent, @V("promote") String promote);

    @SystemMessage("你是文档分析专家，仔细阅读用户上传的所有PDF文件，根据提示词提取文档要点并给出专业回答。{{promote}}")
    String pdfChatWithSystem(@UserMessage String message, @UserMessage List<PdfFileContent> pdfFileContentList, @V("promote") String promote);
}