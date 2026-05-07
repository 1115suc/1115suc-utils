package com.course.minio.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest implements Serializable {

    private String bucketName;
    private String customPath;

    /** 是否生成缩略图 */
    @Builder.Default
    private boolean thumbnail = false;

    /** 缩略图缩放比例，默认 0.5（50%） */
    @Builder.Default
    private float thumbnailScale = 0.5f;

    /** 最大文件大小（字节），-1 表示不限制 */
    @Builder.Default
    private long maxFileSize = -1;
}