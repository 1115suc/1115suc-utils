package com.course.minio.service.Impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.course.minio.entity.enums.PathStrategy;
import com.course.minio.entity.request.FileUploadRequest;
import com.course.minio.entity.request.ImageUploadRequest;
import com.course.minio.entity.response.FileUploadResponse;
import com.course.minio.entity.enums.MinioFileTypeEnum;
import com.course.minio.entity.enums.MinioResponseCodeEnum;
import com.course.minio.exception.CommonException;
import com.course.minio.properties.MinIOConfigProperties;
import com.course.minio.service.MinIOBasicFileService;
import com.course.minio.util.MinioFileStorageUtil;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * MinIO文件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MinIOConfigProperties.class)
public class MinIOBasicFileServiceImpl implements MinIOBasicFileService {

    private static final String SEPARATOR = "/";
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "png", "gif", "webp", "jpeg",
            "pdf", "doc", "docx", "md", "txt", "xls", "xlsx", "ppt", "pptx",
            "html", "css", "js", "mp4", "avi", "mov", "wmv", "flv"
    ));

    private final MinioClient minioClient;
    private final MinIOConfigProperties minIOConfigProperties;
    private final MinioFileStorageUtil minioFileStorageUtil;

    // =========================================================================
    // Bucket 管理
    // =========================================================================

    @Override
    public void checkBucket(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("存储桶已创建: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查存储桶失败: {}", bucketName, e);
            throw new CommonException(MinioResponseCodeEnum.CHECK_BUCKET_FAIL.getMsg(), e);
        }
    }

    @Override
    public void setBucketPolicy(String bucketName, boolean publicRead) {
        bucketName = resolveBucket(bucketName);
        try {
            if (publicRead) {
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\"," +
                                "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"]," +
                                "\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}", bucketName);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            } else {
                minioClient.deleteBucketPolicy(
                        DeleteBucketPolicyArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            log.error("设置存储桶策略失败: {}", bucketName, e);
            throw new CommonException(MinioResponseCodeEnum.SET_POLICY_FAIL.getMsg(), e);
        }
    }

    // =========================================================================
    // 通用文件上传
    // =========================================================================

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest request) {
        validateFile(file);
        if (request.getMaxFileSize() > 0) {
            checkFileSize(file, request.getMaxFileSize());
        }

        String bucketName = resolveBucket(request.getBucketName());
        checkBucket(bucketName);

        String safeFilename = sanitizeOriginalFilename(file.getOriginalFilename());
        verificationFileType(safeFilename);
        String contentType = resolveContentType(file);

        String objectName = switch (request.getPathStrategy()) {
            case UID_FILE_TYPE -> buildUidFileTypePath(request, safeFilename);
            case CUSTOM_PATH   -> buildCustomPath(request.getCustomPath(), safeFilename);
        };

        return doUpload(file, bucketName, objectName, contentType, safeFilename);
    }

    // =========================================================================
    // 图片上传
    // =========================================================================

    @Override
    public FileUploadResponse uploadImage(MultipartFile file, ImageUploadRequest request) {
        validateFile(file);

        String suffix = FileUtil.extName(file.getOriginalFilename());
        if (!StrUtil.equalsAnyIgnoreCase(suffix, "jpg", "png", "gif", "webp", "jpeg")) {
            throw new IllegalArgumentException("无效的图片格式，支持: jpg, png, gif, webp, jpeg");
        }
        if (request.getMaxFileSize() > 0) {
            checkFileSizeWithUnit(file, request.getMaxFileSize());
        }

        // 图片统一走自定义路径策略
        FileUploadRequest uploadRequest = FileUploadRequest.builder()
                .bucketName(request.getBucketName())
                .pathStrategy(PathStrategy.CUSTOM_PATH)
                .customPath(request.getCustomPath())
                .build();

        FileUploadResponse response = uploadFile(file, uploadRequest);

        if (request.isThumbnail()) {
            generateThumbnail(file, resolveBucket(request.getBucketName()),
                    response.getFileName(), suffix, request.getThumbnailScale());
        }
        return response;
    }

    // =========================================================================
    // 下载 / 删除
    // =========================================================================

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        bucketName = resolveBucket(bucketName);
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("下载文件失败: {}", objectName, e);
            throw new CommonException(MinioResponseCodeEnum.DOWNLOAD_FAIL.getMsg(), e);
        }
    }

    @Override
    public void removeFile(String bucketName, String objectName) {
        bucketName = resolveBucket(bucketName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName).object(objectName).build());
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            throw new CommonException(MinioResponseCodeEnum.DELETE_FILE_FAIL.getMsg(), e);
        }
    }

    @Override
    public void removeFiles(String bucketName, List<String> objectNames) {
        bucketName = resolveBucket(bucketName);
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new).collect(Collectors.toList());
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("批量删除出错: {} - {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new CommonException(MinioResponseCodeEnum.DELETE_BATCH_FAIL.getMsg(), e);
        }
    }

    // =========================================================================
    // 路径构建
    // =========================================================================

    private String buildUidFileTypePath(FileUploadRequest request, String safeFilename) {
        if (StrUtil.isBlank(request.getUid())) {
            throw new IllegalArgumentException("pathStrategy=UID_FILE_TYPE 时 uid 不能为空");
        }
        MinioFileTypeEnum fileType = request.getFileType() != null
                ? request.getFileType() : MinioFileTypeEnum.OTHER;
        String dirPrefix = request.getUid() + SEPARATOR + fileType.getDirName();

        if (minIOConfigProperties.isEnableBucketPathPrefix()) {
            return minioFileStorageUtil.buildFilePath(dirPrefix, safeFilename);
        }
        return dirPrefix + SEPARATOR + safeFilename;
    }

    private String buildCustomPath(String customPath, String safeFilename) {
        if (minIOConfigProperties.isEnableBucketPathPrefix()) {
            return minioFileStorageUtil.buildFilePath(customPath, safeFilename);
        }
        if (StrUtil.isNotBlank(customPath)) {
            String prefix = customPath.endsWith(SEPARATOR) ? customPath : customPath + SEPARATOR;
            return prefix + safeFilename;
        }
        return safeFilename;
    }

    // =========================================================================
    // 上传执行
    // =========================================================================

    private FileUploadResponse doUpload(MultipartFile file, String bucketName,
                                        String objectName, String contentType,
                                        String originalFilename) {
        try {
            byte[] bytes = file.getBytes();
            String md5 = SecureUtil.md5(new ByteArrayInputStream(bytes));

            synchronized (this) {
                if (minioFileStorageUtil.checkFileExist(bucketName, objectName)) {
                    log.warn("文件已存在: bucket={}, object={}", bucketName, objectName);
                    throw new MinioException(MinioResponseCodeEnum.FILE_ALREADY_EXISTS.getMsg());
                }
                Map<String, String> metadata = new HashMap<>();
                metadata.put("md5", md5);
                metadata.put("fileSize", String.valueOf(file.getSize()));

                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName).object(objectName)
                        .stream(new ByteArrayInputStream(bytes), file.getSize(), -1)
                        .contentType(contentType).userMetadata(metadata)
                        .build());
                log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
            }

            String fileUrl = String.format("%s/%s/%s",
                    minIOConfigProperties.getEndpoint(), bucketName, objectName);
            return FileUploadResponse.builder()
                    .fileName(objectName).originalName(originalFilename)
                    .fileSize(file.getSize()).fileUrl(fileUrl)
                    .mimeType(contentType).uploadTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new CommonException(MinioResponseCodeEnum.UPLOAD_FAIL.getMsg(), e);
        }
    }

    private void generateThumbnail(MultipartFile file, String bucketName,
                                   String objectName, String suffix, float scale) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImgUtil.scale(file.getInputStream(), out, scale);
            byte[] thumbnailBytes = out.toByteArray();
            String thumbnailObjectName = objectName.replace("." + suffix, "_thumb." + suffix);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName).object(thumbnailObjectName)
                    .stream(new ByteArrayInputStream(thumbnailBytes), thumbnailBytes.length, -1)
                    .contentType(file.getContentType()).build());
            log.info("缩略图已生成: {}", thumbnailObjectName);
        } catch (Exception e) {
            log.warn("缩略图生成失败: {}", file.getOriginalFilename(), e);
        }
    }

    // =========================================================================
    // 工具方法
    // =========================================================================

    private String resolveBucket(String bucketName) {
        return StrUtil.isBlank(bucketName) ? minIOConfigProperties.getBucketName() : bucketName;
    }

    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        return StrUtil.isBlank(ct) ? "application/octet-stream" : ct;
    }

    private void validateFile(MultipartFile file) {
        if (ObjectUtil.isNull(file) || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
    }

    private void checkFileSize(MultipartFile file, long maxFileSize) {
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超过限制: " + maxFileSize + " bytes");
        }
    }

    private void checkFileSizeWithUnit(MultipartFile file, long maxFileSize) {
        if (file.getSize() > maxFileSize) {
            String sizeMsg = maxFileSize >= 1024 * 1024 * 1024
                    ? (maxFileSize / (1024 * 1024 * 1024)) + "GB"
                    : (maxFileSize / (1024 * 1024)) + "MB";
            throw new IllegalArgumentException("文件大小超过限制: " + sizeMsg);
        }
    }

    private void verificationFileType(String safeFilename) {
        String suffix = FileUtil.extName(safeFilename);
        if (StrUtil.isBlank(suffix) || !ALLOWED_EXTENSIONS.contains(suffix.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + suffix);
        }
    }

    private String sanitizeOriginalFilename(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) return "unknown";
        String name = originalFilename;
        try { name = URLDecoder.decode(name, StandardCharsets.UTF_8); } catch (Exception ignore) {}
        name = name.replace("\\", "/");
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) name = name.substring(lastSlash + 1);
        name = name.replaceAll("[\\r\\n\\t]", "_").trim();
        return StrUtil.isBlank(name) ? "unknown" : name;
    }
}