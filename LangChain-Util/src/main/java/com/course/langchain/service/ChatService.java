package com.course.langchain.service;

import dev.langchain4j.data.message.Content;

import java.util.List;

public interface ChatService {
    // 普通聊天
    String chat(String message);

    /** 其他聊天消息
     * @param message 用户聊天消息
     * @param otherMessages 其他聊天消息(图片、视频、PDF文件等)
     * @return 聊天结果
     */
    String chatWithOther(String message, List<Content> otherMessages);
}
