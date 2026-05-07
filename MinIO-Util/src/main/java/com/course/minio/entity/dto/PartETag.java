package com.course.minio.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartETag implements Serializable {

    /** 分片序号（需与上传时一致） */
    private int partNumber;

    /** 上传分片后从响应头 ETag 中获取的值（需去除双引号） */
    private String etag;
}