package com.course.minio.service;

import com.course.minio.entity.request.MultipartUploadCompleteRequest;
import com.course.minio.entity.request.MultipartUploadInitRequest;
import com.course.minio.entity.response.FileUploadResponse;
import com.course.minio.entity.response.MultipartUploadInitResponse;
import com.course.minio.entity.response.MultipartUploadProgressResponse;

public interface MinIOAdvancedFileService {
    /**
     * 获取预览URL（临时签名URL）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    String getPreviewUrl(String bucketName, String objectName);

    /**
     * 获取永久URL（如果存储桶是公开的）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    String getPublicUrl(String bucketName, String objectName);

    /**
     * 初始化分片上传任务
     * 服务端会创建 MinIO 上传任务并为每个分片生成预签名 PUT URL
     * 客户端拿到 URL 后直接向 MinIO 上传，不经过服务端
     */
    MultipartUploadInitResponse initiateMultipartUpload(MultipartUploadInitRequest request);

    /**
     * 合并所有分片，完成上传
     * 客户端需传回所有分片的 partNumber + ETag
     */
    FileUploadResponse completeMultipartUpload(MultipartUploadCompleteRequest request);

    /**
     * 取消分片上传并清理已上传的碎片
     * 建议在上传失败/超时时调用，避免产生存储费用
     */
    void abortMultipartUpload(String objectName, String uploadId, String bucketName);

    /**
     * 查询当前上传进度（已上传了哪些分片）
     *
     * @param totalParts 客户端初始化时的总分片数，用于计算百分比
     */
    MultipartUploadProgressResponse getUploadProgress(String objectName, String uploadId,
                                                      int totalParts, String bucketName);
}
