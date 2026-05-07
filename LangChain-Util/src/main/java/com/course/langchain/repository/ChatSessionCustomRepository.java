package com.course.langchain.repository;

import com.course.langchain.entity.po.ChatSession;

import java.util.List;
import java.util.Optional;

public interface ChatSessionCustomRepository {
    /**
     * 根据会话 ID 查询，messages 按 createdAt 倒序
     */
    Optional<ChatSession> findByIdWithMessagesSortedDesc(String sessionId);

    /**
     * 根据会话 ID 查询，只返回最新 N 条消息（倒序）
     */
    Optional<ChatSession> findByIdWithRecentMessages(String sessionId, int limit);

    /**
     * 根据会话 ID + 用户 UID 查询（鉴权），messages 倒序
     */
    Optional<ChatSession> findByIdAndUserUidWithMessagesSortedDesc(String sessionId, String userUid);

    /**
     * 根据用户 UID 查询所有会话（不含 messages），按 updatedAt 倒序
     */
    List<ChatSession> findByUserUidExcludeMessages(String userUid);
}