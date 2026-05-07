package com.course.langchain.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 聊天消息（嵌套文档，不单独作为 Collection）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDocument {

    /**
     * 消息唯一 ID（ObjectId）
     */
    @Id
    private String id;

    /**
     * 消息角色：USER / AI / SYSTEM
     */
    @Field("role")
    private String role;

    /**
     * 消息内容
     */
    @Field("content")
    private String content;

    /**
     * 消息创建时间
     */
    @Field("createdAt")
    private LocalDateTime createdAt;

    /**
     * 角色枚举常量
     */
    public static final class Role {
        public static final String USER   = "USER";
        public static final String AI     = "AI";
        public static final String SYSTEM = "SYSTEM";

        private Role() {}
    }
}
