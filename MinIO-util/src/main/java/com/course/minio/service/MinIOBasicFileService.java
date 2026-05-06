package com.course.minio.service;

import com.course.minio.entity.dto.FileUploadResponse;
import com.course.minio.entity.enums.MinioFileTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO 文件服务接口
 */
public interface MinIOBasicFileService {

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    void checkBucket(String bucketName);

    /**
     * 设置存储桶策略
     *
     * @param bucketName 存储桶名称
     * @param publicRead true为公开读取，false为私有
     */
    void setBucketPolicy(String bucketName, boolean publicRead);

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName);

    /**
     * 上传通用文件
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param customPath 自定义文件路径
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName, String customPath);

    /**
     * 上传通用文件（重载，支持最大文件大小限制）
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param customPath 自定义文件路径
     * @param maxFileSize 最大文件大小限制（字节），-1表示不限制
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName, String customPath, long maxFileSize);

    /**
     * 上传通用文件（按业务路径规则）
     * 存储路径：{项目名}/{uid}/{fileType}/{originalName}
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param uid        用户uid
     * @param fileType   文件类型
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName, String uid, MinioFileTypeEnum fileType);

    /**
     * 上传通用文件（按业务路径规则）
     * 存储路径：{项目名}/{uid}/{fileType}/{originalName}
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称（可选，为空时使用默认值）
     * @param uid        用户uid
     * @param fileType   文件类型
     * @param maxFileSize 最大文件大小限制（字节），-1表示不限制
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadFile(MultipartFile file, String bucketName, String uid, MinioFileTypeEnum fileType, long maxFileSize);

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadImage(MultipartFile file, String bucketName, boolean thumbnail);

    /**
     * 专门上传图片
     *
     * @param file       MultipartFile文件
     * @param bucketName 存储桶名称
     * @param customPath 自定义路径
     * @param thumbnail  是否生成缩略图
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadImage(MultipartFile file, String bucketName, String customPath, boolean thumbnail);

    /**
     * 专门上传图片（重载，支持最大文件大小限制）
     *
     * @param file        MultipartFile文件
     * @param bucketName  存储桶名称
     * @param customPath  自定义路径
     * @param thumbnail   是否生成缩略图
     * @param maxFileSize 最大文件大小限制（字节），-1表示不限制。例如：10MB = 10 * 1024 * 1024
     * @return FileUploadResponse响应对象
     */
    FileUploadResponse uploadImage(MultipartFile file, String bucketName, String customPath, boolean thumbnail, long maxFileSize);

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
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    void removeFile(String bucketName, String objectName);

    /**
     * 批量删除文件
     *
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     */
    void removeFiles(String bucketName, List<String> objectNames);
}
