package com.course.minio.entity.request;

import com.course.minio.entity.enums.MinioFileTypeEnum;
import com.course.minio.entity.enums.PathStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequest implements Serializable {

    /** 指定存储桶，不传则使用配置默认桶 */
    private String bucketName;

    /**
     * 路径策略
     * CUSTOM_PATH   → [customPath/][yyyy/MM/dd/]{filename}
     * UID_FILE_TYPE → {uid}/{fileType.dirName}/[yyyy/MM/dd/]{filename}
     */
    @Builder.Default
    private PathStrategy pathStrategy = PathStrategy.CUSTOM_PATH;

    // ── 策略二：CUSTOM_PATH ──────────────────────────────────────────────────
    /** 自定义目录前缀，不传则直接以文件名作为 objectName */
    private String customPath;

    // ── 策略一：UID_FILE_TYPE ────────────────────────────────────────────────
    /** 用户 UID，pathStrategy=UID_FILE_TYPE 时必填 */
    private String uid;

    /** 文件类型，不传默认 OTHER */
    private MinioFileTypeEnum fileType;

    // ── 通用限制 ─────────────────────────────────────────────────────────────
    /** 最大文件大小（字节），-1 表示不限制 */
    @Builder.Default
    private long maxFileSize = -1;
}