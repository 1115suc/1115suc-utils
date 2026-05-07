package com.course.minio.entity.response;

import com.course.minio.entity.dto.PartInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadInitResponse implements Serializable {

    /** MinIO 返回的上传任务 ID，完成/取消时必须携带 */
    private String uploadId;

    /** 服务端生成的对象名（含路径前缀），后续请求需原样传回 */
    private String objectName;

    /** 存储桶名称 */
    private String bucketName;

    /** 总分片数 */
    private int totalParts;

    /** 实际使用的分片大小（字节） */
    private long chunkSize;

    /** 各分片信息，按 partNumber 升序排列 */
    private List<PartInfo> parts;

    /** 预签名 URL 过期时间 */
    private LocalDateTime expireTime;
}