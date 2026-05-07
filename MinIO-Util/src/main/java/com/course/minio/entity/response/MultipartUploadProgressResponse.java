package com.course.minio.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadProgressResponse implements Serializable {

    private String uploadId;
    private String objectName;

    /** 预期总分片数（由客户端传入，MinIO 侧无法单独查到） */
    private int totalParts;

    /** 已上传的分片数 */
    private int uploadedParts;

    /** 上传进度百分比（保留两位小数） */
    private double progressPercent;

    /** 已上传字节数 */
    private long uploadedSize;

    /** 已上传的分片编号列表 */
    private List<Integer> uploadedPartNumbers;
}