package com.course.minio.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件上传类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse implements Serializable {

    /**
     * 唯一文件标识（对象名称）
     */
    private String fileId;

    /**
     * 存储的文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private long fileSize;

    /**
     * 访问 URL
     */
    private String fileUrl;

    /**
     * MIME 类型
     */
    private String mimeType;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}