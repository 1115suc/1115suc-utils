package com.course.minio.service;

import com.course.minio.entity.request.FileUploadRequest;
import com.course.minio.entity.request.ImageUploadRequest;
import com.course.minio.entity.response.FileUploadResponse;
import com.course.minio.entity.enums.MinioFileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO 文件服务接口
 */
public interface MinIOBasicFileService {

    /**
     * 通用文件上传（统一入口）
     * 路径策略由 request.pathStrategy 决定
     */
    FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest request);

    /**
     * 图片上传（含可选缩略图生成）
     */
    FileUploadResponse uploadImage(MultipartFile file, ImageUploadRequest request);

    /**
     * 下载文件
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 删除单个文件
     */
    void removeFile(String bucketName, String objectName);

    /**
     * 批量删除文件
     */
    void removeFiles(String bucketName, List<String> objectNames);

    /**
     * 检查/创建存储桶
     */
    void checkBucket(String bucketName);

    /**
     * 设置存储桶访问策略
     */
    void setBucketPolicy(String bucketName, boolean publicRead);
}
