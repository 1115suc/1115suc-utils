package com.course.minio.util;

import com.course.minio.entity.enums.MinioResponseCodeEnum;
import com.course.minio.exception.CommonException;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioFileStorageUtil {

    private final MinioClient minioClient;
    private final static String separator = "/";

    /**
     * 构建文件存储路径
     * @param dirPrefix 目录前缀，如果为空则不添加前缀
     * @param filename 文件名
     * @return 完整路径: [dirPrefix/]yyyy/MM/dd/filename
     */
    public String buildFilePath(String dirPrefix, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (dirPrefix != null && !dirPrefix.isEmpty()) {
            stringBuilder.append(dirPrefix);
            if (!dirPrefix.endsWith(separator)) {
                stringBuilder.append(separator);
            }
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String todayString = simpleDateFormat.format(new Date());
        stringBuilder.append(todayString)
                .append(separator)
                .append(filename);
        return stringBuilder.toString();
    }

    /**
     * 检查文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return true: 存在, false: 不存在
     */
    public boolean checkFileExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            // ErrorResponseException 包含错误码，如果为 NoSuchKey 则表示文件不存在
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            // 其他错误可能也意味着无法访问，但在本场景下如果不确定，暂时抛出异常或返回 false 需谨慎
            // 这里为了简单起见，假设 NoSuchKey 是唯一的“不存在”错误
            log.warn("检查文件是否存在MinIO错误: code={}, msg={}", e.errorResponse().code(), e.errorResponse().message());
            return false;
        } catch (Exception e) {
            // 其他异常，如网络错误
            log.warn("检查文件是否存在异常: {}", objectName, e);
            throw new CommonException(MinioResponseCodeEnum.FILE_EXCEPTION.getMsg(), e);
        }
    }
}