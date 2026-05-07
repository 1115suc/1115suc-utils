db.createCollection("chat_memory", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["userUid", "createdAt", "updatedAt"],
      properties: {
        _id: {
          bsonType: "objectId",
          description: "主键，自动生成"
        },
        userUid: {
          bsonType: "string",
          description: "用户唯一标识，必填"
        },
        title: {
          bsonType: "string",
          description: "会话标题"
        },
        aiModelId: {
          bsonType: ["string", "default"],
          description: "AI 模型 ID，为 default 时使用系统默认"
        },
        createdAt: {
          bsonType: "date",
          description: "创建时间，必填"
        },
        updatedAt: {
          bsonType: "date",
          description: "更新时间，必填"
        },
        // 嵌套的聊天消息数组
        messages: {
          bsonType: "array",
          description: "嵌套的聊天消息列表",
          items: {
            bsonType: "object",
            required: ["role", "content", "createdAt"],
            properties: {
              _id: {
                bsonType: "objectId",
                description: "消息唯一 ID"
              },
              role: {
                bsonType: "string",
                enum: ["USER", "AI", "SYSTEM"],
                description: "消息角色，枚举值：USER / AI / SYSTEM"
              },
              content: {
                bsonType: "string",
                description: "消息内容"
              },
              createdAt: {
                bsonType: "date",
                description: "消息创建时间，必填"
              }
            }
          }
        }
      }
    }
  }
})

// 按用户 UID 查询会话
db.chat_memory.createIndex({ userUid: 1 }, { name: "idx_userUid" })

// 按创建时间倒序排列
db.chat_memory.createIndex({ createdAt: -1 }, { name: "idx_createdAt" })

// 复合索引：用户 + 时间
db.chat_memory.createIndex(
  { userUid: 1, updatedAt: -1 },
  { name: "idx_userUid_updatedAt" }
)