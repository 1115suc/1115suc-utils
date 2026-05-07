package com.course.langchain.handler;

import com.course.langchain.entity.po.ChatMessageDocument;
import com.course.langchain.repository.ChatSessionRepository;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoMemoryStore implements ChatMemoryStore {

    private final ChatSessionRepository chatSessionRepository;

    /**
     * memoryId 即 sessionId (String)
     * 从 MongoDB 加载消息，并转换为 Langchain4j ChatMessage 列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sessionId = toSessionId(memoryId);
        log.debug("[MongoMemoryStore] 获取消息，sessionId={}", sessionId);

        return chatSessionRepository.findById(sessionId)
                .map(session -> session.getMessages().stream()
                        .map(this::toChatMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .orElseGet(() -> {
                    log.warn("[MongoMemoryStore] 未找到会话：{}", sessionId);
                    return new ArrayList<>();
                });
    }

    /**
     * Langchain4j 每次对话后回调此方法，将最新完整消息列表覆盖写入 MongoDB
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = toSessionId(memoryId);
        chatSessionRepository.findById(sessionId).ifPresentOrElse(
                session -> {
                    Map<String, Deque<LocalDateTime>> existingTimeMap = buildExistingTimeMap(session.getMessages());

                    List<ChatMessageDocument> docs = messages.stream()
                            .map(msg -> toChatMessageDocument(msg, existingTimeMap))
                            .collect(Collectors.toList());

                    session.setMessages(docs);
                    session.setUpdatedAt(LocalDateTime.now());
                    chatSessionRepository.save(session);
                    log.debug("[MongoMemoryStore] 更新 {} 条消息，session: {}", docs.size(), sessionId);
                },
                () -> log.warn("[MongoMemoryStore] 更新消息 - Session 不存在：{}", sessionId)
        );
    }


    /**
     * 清空会话消息（不删除会话本身）
     */
    @Override
    public void deleteMessages(Object memoryId) {
        String sessionId = toSessionId(memoryId);
        chatSessionRepository.findById(sessionId).ifPresentOrElse(
                session -> {
                    session.setMessages(new ArrayList<>());
                    session.setUpdatedAt(LocalDateTime.now());
                    chatSessionRepository.save(session);
                    log.info("[MongoMemoryStore] 已删除会话的所有消息：{}", sessionId);
                },
                () -> log.warn("[MongoMemoryStore] 删除消息 - 未找到会话：{}", sessionId)
        );
    }

    // ----------------------------------------------------------------
    // 类型转换：ChatMessageDocument → Langchain4j ChatMessage
    // ----------------------------------------------------------------
    private ChatMessage toChatMessage(ChatMessageDocument doc) {
        if (doc.getContent() == null) return null;
        return switch (doc.getRole()) {
            case ChatMessageDocument.Role.USER   -> UserMessage.from(doc.getContent());
            case ChatMessageDocument.Role.AI     -> AiMessage.from(doc.getContent());
            case ChatMessageDocument.Role.SYSTEM -> SystemMessage.from(doc.getContent());
            default -> {
                log.warn("[MongoMemoryStore] 未知的角色类型 '{}', 跳过", doc.getRole());
                yield null;
            }
        };
    }

    // ----------------------------------------------------------------
    // 类型转换：Langchain4j ChatMessage → ChatMessageDocument
    // ----------------------------------------------------------------
    /**
     * 转换时优先复用旧时间，无匹配才使用当前时间
     */
    private ChatMessageDocument toChatMessageDocument(
            ChatMessage message, Map<String, Deque<LocalDateTime>> existingTimeMap) {

        String role;
        String content;

        if (message instanceof UserMessage userMsg) {
            role    = ChatMessageDocument.Role.USER;
            content = userMsg.singleText();
        } else if (message instanceof AiMessage aiMsg) {
            role    = ChatMessageDocument.Role.AI;
            content = aiMsg.text();
        } else if (message instanceof SystemMessage sysMsg) {
            role    = ChatMessageDocument.Role.SYSTEM;
            content = sysMsg.text();
        } else {
            throw new RuntimeException("[MongoMemoryStore] 不支持的消息类型：" + message.getClass().getName());
        }

        String key = role + "::" + content;
        Deque<LocalDateTime> times = existingTimeMap.get(key);
        LocalDateTime createdAt = (times != null && !times.isEmpty()) ? times.poll() : LocalDateTime.now();

        return ChatMessageDocument.builder()
                .id(new ObjectId().toHexString())
                .role(role)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

    private ChatMessageDocument toChatMessageDocument(ChatMessage message) {
        return toChatMessageDocument(message, Map.of());
    }

    /**
     * 构建已有消息的时间映射
     * key = "role::content"，value = 按原始顺序排列的时间队列
     * 用队列处理相同 role+content 的多条消息（如重复问同一句话）
     */
    private Map<String, Deque<LocalDateTime>> buildExistingTimeMap(List<ChatMessageDocument> existing) {
        Map<String, Deque<LocalDateTime>> map = new LinkedHashMap<>();
        if (existing == null) return map;
        for (ChatMessageDocument doc : existing) {
            String key = doc.getRole() + "::" + doc.getContent();
            map.computeIfAbsent(key, k -> new ArrayDeque<>()).offer(doc.getCreatedAt());
        }
        return map;
    }

    private String toSessionId(Object memoryId) {
        if (memoryId == null) throw new RuntimeException("[MongoMemoryStore] memoryId 不能为空");
        return memoryId.toString();
    }
}
