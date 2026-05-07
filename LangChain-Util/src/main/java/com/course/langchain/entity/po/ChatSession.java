package com.course.langchain.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话（主文档），内嵌 ChatMessage 列表
 * 对应 MongoDB Collection: chat_memory
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_memory")
@CompoundIndexes({
    @CompoundIndex(name = "idx_userUid_updatedAt", def = "{'userUid': 1, 'updatedAt': -1}")
})
public class ChatSession {

    @Id
    private String id;

    /**
     * 用户唯一标识
     */
    @Indexed(name = "idx_userUid")
    @Field("userUid")
    private String userUid;

    /**
     * 会话标题
     */
    @Field("title")
    private String title;

    /**
     * AI 模型 ID，为 null 时使用系统默认
     */
    @Field("aiModelId")
    private Long aiModelId;

    /**
     * 创建时间
     */
    @Indexed(name = "idx_createdAt")
    @Field("createdAt")
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @Field("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * 嵌套的聊天消息列表（核心：将 chat_message 内嵌到会话中）
     */
    @Field("messages")
    @Builder.Default
    private List<ChatMessageDocument> messages = new ArrayList<>();
}