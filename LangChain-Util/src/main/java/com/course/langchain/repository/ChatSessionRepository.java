package com.course.langchain.repository;

import com.course.langchain.entity.po.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ChatSession MongoDB Repository
 */
@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String>, ChatSessionCustomRepository{
    /**
     * 查询用户所有会话（按更新时间倒序，不返回 messages 字段，节省带宽）
     */
    @Query(value = "{ 'user_uid': ?0 }", fields = "{ 'messages': 0 }")
    List<ChatSession> findByUserUidExcludeMessages(String userUid);

    /**
     * 查询用户所有会话（含 messages）
     */
    List<ChatSession> findByUserUidOrderByUpdatedAtDesc(String userUid);

    /**
     * 根据会话 ID 和用户 UID 查询（鉴权用，防越权）
     */
    Optional<ChatSession> findByIdAndUserUid(String id, String userUid);

    /**
     * 删除某用户的全部会话
     */
    void deleteByUserUid(String userUid);

    /**
     * 统计某用户的会话数量
     */
    long countByUserUid(String userUid);
}