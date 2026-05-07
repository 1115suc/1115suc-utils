package com.course.langchain.repository.Impl;

import com.course.langchain.entity.po.ChatSession;
import com.course.langchain.repository.ChatSessionCustomRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatSessionCustomRepositoryImpl implements ChatSessionCustomRepository {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION = "chat_memory";

    @Override
    public Optional<ChatSession> findByIdWithMessagesSortedDesc(String sessionId) {
        Aggregation aggregation = buildAggregation(
                Aggregation.match(Criteria.where("_id").is(new ObjectId(sessionId))),
                -1, null
        );
        return queryUnique(aggregation);
    }

    @Override
    public Optional<ChatSession> findByIdWithRecentMessages(String sessionId, int limit) {
        Aggregation aggregation = buildAggregation(
                Aggregation.match(Criteria.where("_id").is(new ObjectId(sessionId))),
                -1, limit
        );
        return queryUnique(aggregation);
    }

    @Override
    public Optional<ChatSession> findByIdAndUserUidWithMessagesSortedDesc(
            String sessionId, String userUid) {
        Aggregation aggregation = buildAggregation(
                Aggregation.match(
                        Criteria.where("_id").is(new ObjectId(sessionId))
                                .and("userUid").is(userUid)
                ),
                -1, null
        );
        return queryUnique(aggregation);
    }

    @Override
    public List<ChatSession> findByUserUidExcludeMessages(String userUid) {
        // 直接查询，排除 messages 字段
        Query query = new Query();
        query.addCriteria(Criteria.where("userUid").is(userUid));
        query.fields().exclude("messages");
        query.with(Sort.by(Sort.Direction.DESC, "updatedAt"));

        return mongoTemplate.find(query, ChatSession.class, COLLECTION);
    }

    // ==================== 核心管道构建 ====================

    private Aggregation buildAggregation(MatchOperation match, int sortDirection, Integer limit) {
        // 1. 匹配文档
        // 2. 拆散 messages 数组（保留没有 messages 的文档）
        UnwindOperation unwind = Aggregation.unwind("messages", true);

        // 3. 按 messages.createdAt 排序（决定升/降序）
        SortOperation sort = sortDirection > 0
                ? Aggregation.sort(Sort.Direction.ASC, "messages.createdAt")
                : Aggregation.sort(Sort.Direction.DESC, "messages.createdAt");

        // 4. 按会话主键重新聚合，pushAll 恢复 messages 数组
        GroupOperation group = Aggregation.group("_id")
                .first("userUid").as("userUid")
                .first("title").as("title")
                .first("aiModelId").as("aiModelId")
                .first("createdAt").as("createdAt")
                .first("updatedAt").as("updatedAt")
                .push("messages").as("messages");

        if (limit != null) {
            // 5. 截取前 N 条
            AggregationOperation slice = sliceMessagesStage(limit);
            return Aggregation.newAggregation(match, unwind, sort, group, slice);
        }
        return Aggregation.newAggregation(match, unwind, sort, group);
    }

    private Optional<ChatSession> queryUnique(Aggregation aggregation) {
        AggregationResults<ChatSession> results =
                mongoTemplate.aggregate(aggregation, COLLECTION, ChatSession.class);
        return Optional.ofNullable(results.getUniqueMappedResult());
    }

    /**
     * $set: messages 截取前 N 条（$slice 在 MongoDB 3.2+ 均支持）
     */
    private AggregationOperation sliceMessagesStage(int limit) {
        return context -> new Document("$set",
                new Document("messages",
                        new Document("$slice", Arrays.asList("$messages", limit))
                )
        );
    }
}