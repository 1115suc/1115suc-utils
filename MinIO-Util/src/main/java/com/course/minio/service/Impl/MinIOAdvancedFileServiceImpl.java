package com.course.minio.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.course.minio.entity.dto.PartInfo;
import com.course.minio.entity.dto.PartETag;
import com.course.minio.entity.enums.MinioFileTypeEnum;
import com.course.minio.entity.enums.MinioResponseCodeEnum;
import com.course.minio.entity.request.MultipartUploadCompleteRequest;
import com.course.minio.entity.request.MultipartUploadInitRequest;
import com.course.minio.entity.response.FileUploadResponse;
import com.course.minio.entity.response.MultipartUploadInitResponse;
import com.course.minio.entity.response.MultipartUploadProgressResponse;
import com.course.minio.exception.CommonException;
import com.course.minio.properties.MinIOConfigProperties;
import com.course.minio.service.MinIOAdvancedFileService;
import com.course.minio.util.MinioFileStorageUtil;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.io.File.separator;

@Slf4j
@Service
@ConditionalOnBean(MinIOConfigProperties.class)
public class MinIOAdvancedFileServiceImpl implements MinIOAdvancedFileService {

    /**
     * S3 协议强制下限，低于此值 MinIO 会拒绝（最后一片除外）
     */
    private static final long ABSOLUTE_MIN_CHUNK_SIZE = 5 * 1024 * 1024L;
    private static final int MAX_PARTS = 10_000;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final MinioClient minioClient;
    private final MinIOConfigProperties properties;
    private final MinioFileStorageUtil minioFileStorageUtil;

    /**
     * 用于并发生成预签名 URL，线程数来自配置
     */
    private final ExecutorService presignExecutor;

    public MinIOAdvancedFileServiceImpl(S3Client s3Client,
                                        S3Presigner s3Presigner,
                                        MinioClient minioClient,
                                        MinIOConfigProperties properties,
                                        MinioFileStorageUtil minioFileStorageUtil) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.minioClient = minioClient;
        this.properties = properties;
        this.minioFileStorageUtil = minioFileStorageUtil;
        this.presignExecutor = Executors.newFixedThreadPool(
                properties.getMultipartThreadPoolSize(),
                r -> {
                    Thread t = new Thread(r, "minio-presign-");
                    t.setDaemon(true);
                    return t;
                }
        );
        log.info("[分片上传] 预签名线程池初始化完成，线程数: {}", properties.getMultipartThreadPoolSize());
    }

    /**
     * 获取预览URL（临时签名URL）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return URL字符串
     */
    public String getPreviewUrl(String bucketName, String objectName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = properties.getBucketName();
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(properties.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预览URL失败: {}", objectName, e);
            throw new CommonException(MinioResponseCodeEnum.GET_PREVIEW_URL_FAIL.getMsg(), e);
        }
    }

    /**
     * 获取永久URL（如果存储桶是公开的）
     */
    public String getPublicUrl(String bucketName, String objectName) {
        if (StrUtil.isBlank(bucketName)) {
            bucketName = properties.getBucketName();
        }
        return String.format("%s/%s/%s", properties.getEndpoint(), bucketName, objectName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. 初始化
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public MultipartUploadInitResponse initiateMultipartUpload(MultipartUploadInitRequest request) {
        String bucket = resolveBucket(request.getBucketName());
        long chunkSize = resolveChunkSize(request.getChunkSize());
        long fileSize = request.getFileSize();

        // multipartThreshold：文件未达到阈值时拒绝发起分片上传，引导使用普通上传
        if (fileSize < properties.getMultipartThreshold()) {
            throw new IllegalArgumentException(String.format(
                    "文件大小 %d bytes 未超过分片上传阈值 %d bytes，请使用普通上传接口",
                    fileSize, properties.getMultipartThreshold()));
        }

        int totalParts = (int) Math.ceil((double) fileSize / chunkSize);
        if (totalParts > MAX_PARTS) {
            throw new IllegalArgumentException(String.format(
                    "分片数 %d 超过上限 %d，请增大 chunkSize", totalParts, MAX_PARTS));
        }

        String objectName = generateObjectName(request);
        String contentType = StrUtil.isBlank(request.getContentType())
                ? "application/octet-stream" : request.getContentType();
        // urlExpiry：不传时使用配置默认值
        int urlExpiry = request.getUrlExpiry() != null
                ? request.getUrlExpiry() : properties.getPresignedUrlExpiry();

        CreateMultipartUploadResponse awsResp = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(objectName)
                        .contentType(contentType)
                        .build()
        );
        String uploadId = awsResp.uploadId();
        log.info("[分片上传] 任务创建 uploadId={} object={} parts={} chunkSize={}MB",
                uploadId, objectName, totalParts, chunkSize / 1024 / 1024);

        // 并发生成预签名 URL
        List<PartInfo> parts = buildPartInfoListConcurrently(
                bucket, objectName, uploadId, totalParts, chunkSize, fileSize, urlExpiry);

        return MultipartUploadInitResponse.builder()
                .uploadId(uploadId)
                .objectName(objectName)
                .bucketName(bucket)
                .totalParts(totalParts)
                .chunkSize(chunkSize)
                .parts(parts)
                .expireTime(LocalDateTime.now().plusSeconds(urlExpiry))
                .build();
    }

    /**
     * 并发生成所有分片的预签名 URL
     * 分片数较多时（如 1000 片）串行生成会很慢，并发生成可显著提速
     */
    private List<PartInfo> buildPartInfoListConcurrently(String bucket, String objectName,
                                                         String uploadId, int totalParts,
                                                         long chunkSize, long fileSize,
                                                         int urlExpiry) {
        List<CompletableFuture<PartInfo>> futures = new ArrayList<>(totalParts);

        for (int i = 1; i <= totalParts; i++) {
            final int partNumber = i;
            final long startByte = (long) (i - 1) * chunkSize;
            final long endByte = Math.min(startByte + chunkSize - 1, fileSize - 1);
            final long partSize = endByte - startByte + 1;

            // 每个分片的预签名 URL 生成提交到线程池
            CompletableFuture<PartInfo> future = CompletableFuture.supplyAsync(() -> {
                PresignedUploadPartRequest presigned = s3Presigner.presignUploadPart(
                        UploadPartPresignRequest.builder()
                                .signatureDuration(Duration.ofSeconds(urlExpiry))
                                .uploadPartRequest(UploadPartRequest.builder()
                                        .bucket(bucket)
                                        .key(objectName)
                                        .uploadId(uploadId)
                                        .partNumber(partNumber)
                                        .contentLength(partSize)
                                        .build())
                                .build()
                );
                return PartInfo.builder()
                        .partNumber(partNumber)
                        .presignedUrl(presigned.url().toString())
                        .startByte(startByte)
                        .endByte(endByte)
                        .partSize(partSize)
                        .build();
            }, presignExecutor);

            futures.add(future);
        }

        // 等待所有分片 URL 生成完毕，按 partNumber 升序返回
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        throw new RuntimeException("预签名 URL 生成失败", e);
                    }
                })
                .sorted(Comparator.comparingInt(PartInfo::getPartNumber))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. 合并分片
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public FileUploadResponse completeMultipartUpload(MultipartUploadCompleteRequest request) {
        String bucket = resolveBucket(request.getBucketName());

        List<CompletedPart> completedParts = request.getParts().stream()
                .sorted(Comparator.comparingInt(PartETag::getPartNumber))
                .map(p -> CompletedPart.builder()
                        .partNumber(p.getPartNumber())
                        .eTag(normalizeETag(p.getEtag()))
                        .build())
                .collect(Collectors.toList());

        // 全部 public API
        s3Client.completeMultipartUpload(
                software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(request.getObjectName())
                        .uploadId(request.getUploadId())
                        .multipartUpload(CompletedMultipartUpload.builder()
                                .parts(completedParts)
                                .build())
                        .build()
        );
        log.info("[分片上传] 合并完成 uploadId={} object={}", request.getUploadId(), request.getObjectName());

        // 查询文件元信息
        HeadObjectResponse head = s3Client.headObject(
                HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(request.getObjectName())
                        .build()
        );

        return FileUploadResponse.builder()
                .fileName(request.getObjectName())
                .originalName(extractFilename(request.getObjectName()))
                .fileSize(head.contentLength())
                .fileUrl(buildFileUrl(bucket, request.getObjectName()))
                .mimeType(head.contentType())
                .uploadTime(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. 取消上传
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void abortMultipartUpload(String objectName, String uploadId, String bucketName) {
        String bucket = resolveBucket(bucketName);
        // public API
        s3Client.abortMultipartUpload(
                AbortMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(objectName)
                        .uploadId(uploadId)
                        .build()
        );
        log.info("[分片上传] 已取消 uploadId={}", uploadId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. 查询进度（已上传了哪些分片）
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public MultipartUploadProgressResponse getUploadProgress(String objectName, String uploadId,
                                                             int totalParts, String bucketName) {
        String bucket = resolveBucket(bucketName);
        ListPartsResponse listResp = s3Client.listParts(
                ListPartsRequest.builder()
                        .bucket(bucket)
                        .key(objectName)
                        .uploadId(uploadId)
                        .maxParts(MAX_PARTS)
                        .build()
        );

        List<software.amazon.awssdk.services.s3.model.Part> uploaded = listResp.parts();
        List<Integer> uploadedNumbers = uploaded.stream()
                .map(software.amazon.awssdk.services.s3.model.Part::partNumber)
                .collect(Collectors.toList());
        long uploadedSize = uploaded.stream()
                .mapToLong(software.amazon.awssdk.services.s3.model.Part::size)
                .sum();
        double progress = totalParts > 0
                ? Math.round((double) uploadedNumbers.size() / totalParts * 10000) / 100.0 : 0.0;

        return MultipartUploadProgressResponse.builder()
                .uploadId(uploadId)
                .objectName(objectName)
                .totalParts(totalParts)
                .uploadedParts(uploadedNumbers.size())
                .progressPercent(progress)
                .uploadedSize(uploadedSize)
                .uploadedPartNumbers(uploadedNumbers)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 解析分片大小：
     * 1. 优先使用 Request 中显式传入的值
     * 2. 未传则使用配置的 multipartChunkSize
     * 3. 两者都不得低于 S3 协议 5MB 下限
     */
    private long resolveChunkSize(Long requested) {
        long size = (requested != null) ? requested : properties.getMultipartChunkSize();
        if (size < ABSOLUTE_MIN_CHUNK_SIZE) {
            log.warn("[分片上传] chunkSize={}bytes 低于 S3 最小限制，已自动调整为 5MB", size);
            return ABSOLUTE_MIN_CHUNK_SIZE;
        }
        return size;
    }

    private String resolveBucket(String bucketName) {
        return StrUtil.isBlank(bucketName) ? properties.getBucketName() : bucketName;
    }

    /**
     * 根据 Request 中的路径策略生成 objectName
     */
    private String generateObjectName(MultipartUploadInitRequest request) {
        return switch (request.getPathStrategy()) {
            case UID_FILE_TYPE -> buildUidFileTypePath(request);
            case CUSTOM_PATH -> buildCustomPath(request);
        };
    }

    /**
     * 策略一：{uid}/{fileType.dirName}/{fileName}
     * 若 enableBucketPathPrefix=true，则在 fileType 目录后插入 yyyy/MM/dd
     * 最终路径示例：
     * 关闭日期前缀 → user123/image/avatar.png
     * 开启日期前缀 → user123/image/2026/05/07/avatar.png
     */
    private String buildUidFileTypePath(MultipartUploadInitRequest request) {
        if (StrUtil.isBlank(request.getUid())) {
            throw new IllegalArgumentException("pathStrategy=UID_FILE_TYPE 时 uid 不能为空");
        }

        MinioFileTypeEnum fileType = request.getFileType() != null
                ? request.getFileType() : MinioFileTypeEnum.OTHER;

        String safeFilename = sanitizeFilename(request.getFileName());

        if (properties.isEnableBucketPathPrefix()) {
            // uid/fileType/yyyy/MM/dd/filename
            return minioFileStorageUtil.buildFilePath(
                    request.getUid() + separator + fileType.getDirName(),
                    safeFilename
            );
        }
        // uid/fileType/filename
        return request.getUid() + separator + fileType.getDirName() + separator + safeFilename;
    }

    /**
     * 策略二：[customPath/][yyyy/MM/dd/]{fileName}
     * 若 enableBucketPathPrefix=true，通过 buildFilePath 自动叠加日期
     * 最终路径示例：
     * 有 customPath + 关闭日期前缀 → docs/report.pdf
     * 有 customPath + 开启日期前缀 → docs/2026/05/07/report.pdf
     * 无 customPath + 开启日期前缀 → 2026/05/07/report.pdf
     */
    private String buildCustomPath(MultipartUploadInitRequest request) {
        String safeFilename = sanitizeFilename(request.getFileName());

        if (properties.isEnableBucketPathPrefix()) {
            // buildFilePath 内部会自动拼接 yyyy/MM/dd，customPath 可为 null
            return minioFileStorageUtil.buildFilePath(request.getCustomPath(), safeFilename);
        }

        // 不开启日期前缀，直接拼接
        if (StrUtil.isNotBlank(request.getCustomPath())) {
            String prefix = request.getCustomPath().endsWith(separator)
                    ? request.getCustomPath()
                    : request.getCustomPath() + separator;
            return prefix + safeFilename;
        }

        return safeFilename;
    }

    /**
     * 文件名安全处理：去路径、去控制字符、保留扩展名
     * 分片上传不用 UUID 重命名，保持原始文件名（调用方已在路径上做了唯一性区分）
     */
    private String sanitizeFilename(String filename) {
        if (StrUtil.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        try {
            filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);
        } catch (Exception ignore) {
        }

        filename = filename.replace("\\", "/");
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash >= 0) {
            filename = filename.substring(lastSlash + 1);
        }
        filename = filename.replaceAll("[\\r\\n\\t]", "_").trim();
        return StrUtil.isBlank(filename) ? "unknown" : filename;
    }

    private String buildFileUrl(String bucket, String objectName) {
        return properties.getEndpoint() + "/" + bucket + "/" + objectName;
    }

    private String extractFilename(String objectName) {
        int idx = objectName.lastIndexOf('/');
        return idx >= 0 ? objectName.substring(idx + 1) : objectName;
    }

    /**
     * 去除 ETag 中 S3 返回的双引号，MinIO 合并时需要干净的值
     */
    private String normalizeETag(String etag) {
        return etag != null ? etag.replace("\"", "") : null;
    }
}
