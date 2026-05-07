package com.course.minio.entity.request;

import com.course.minio.entity.enums.MinioFileTypeEnum;
import com.course.minio.entity.enums.PathStrategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadInitRequest implements Serializable {

    /** 原始文件名，必填 */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /** 文件总大小（字节），必填 */
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须为正数")
    private Long fileSize;

    /** 每个分片大小（字节），不传则使用配置值 */
    private Long chunkSize;

    /** 文件 MIME 类型，不传则识别为 application/octet-stream */
    private String contentType;

    /** 指定存储桶，不传则使用配置默认桶 */
    private String bucketName;

    /** 预签名 URL 有效期（秒），不传则使用配置值 */
    private Integer urlExpiry;

    // ── 路径策略 ──────────────────────────────────────────────────────────────

    /**
     * 路径策略，决定 objectName 的生成方式
     * UID_FILE_TYPE : {uid}/{fileType}/{fileName}  （可选叠加日期前缀）
     * CUSTOM_PATH   : {customPath}/{fileName}       （可选叠加日期前缀）
     * 不传默认 CUSTOM_PATH
     */
    @Builder.Default
    private PathStrategy pathStrategy = PathStrategy.CUSTOM_PATH;

    /**
     * 用户 UID，pathStrategy = UID_FILE_TYPE 时必填
     */
    private String uid;

    /**
     * 文件类型枚举，pathStrategy = UID_FILE_TYPE 时必填
     * 不传默认 OTHER
     */
    private MinioFileTypeEnum fileType;

    /**
     * 自定义目录前缀，pathStrategy = CUSTOM_PATH 时使用
     * 不传则直接以文件名作为 objectName
     */
    private String customPath;
}