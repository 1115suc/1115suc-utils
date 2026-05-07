package com.course.minio.entity.request;

import com.course.minio.entity.dto.PartETag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class MultipartUploadCompleteRequest implements Serializable {

    /** 初始化时返回的 uploadId */
    @NotBlank(message = "uploadId 不能为空")
    private String uploadId;

    /** 初始化时返回的 objectName */
    @NotBlank(message = "objectName 不能为空")
    private String objectName;

    /** 指定存储桶，不传则使用配置默认桶 */
    private String bucketName;

    /** 所有已上传分片的 ETag 信息（无需客户端排序，服务端处理） */
    @NotEmpty(message = "分片 ETag 列表不能为空")
    private List<PartETag> parts;
}