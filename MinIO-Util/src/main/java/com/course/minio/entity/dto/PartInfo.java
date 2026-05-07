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
public class PartInfo implements Serializable {

    /** 分片序号（从 1 开始） */
    private int partNumber;

    /** 该分片的 PUT 预签名 URL，客户端直接用此 URL 上传 */
    private String presignedUrl;

    /** 该分片在文件中的起始字节位置（用于客户端切片） */
    private long startByte;

    /** 该分片在文件中的结束字节位置（含） */
    private long endByte;

    /** 该分片的实际大小（字节） */
    private long partSize;
}