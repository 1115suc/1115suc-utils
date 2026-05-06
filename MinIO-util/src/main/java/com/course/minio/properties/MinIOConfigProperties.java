package com.course.minio.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;

/**
 * MinIO 配置属性
 */
@Data
@Validated
@ConfigurationProperties(prefix = "minio")
@ConditionalOnProperty(prefix = "minio", value = "endpoint")
public class MinIOConfigProperties {

    /**
     * MinIO 服务终端地址
     */
    @NotBlank(message = "MinIO 终端地址不能为空")
    @Pattern(regexp = "^(http|https)://.*$", message = "MinIO 终端地址必须以 http 或 https 开头")
    private String endpoint;

    /**
     * MinIO 访问密钥 (Access Key)
     */
    @NotBlank(message = "MinIO 访问密钥不能为空")
    private String accessKey;

    /**
     * MinIO 安全密钥 (Secret Key)
     */
    @NotBlank(message = "MinIO 安全密钥不能为空")
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    @NotBlank(message = "MinIO 存储桶名称不能为空")
    private String bucketName;

    /**
     * 是否开启存储桶路径日期前缀
     */
    private boolean enableBucketPathPrefix = false;

    /**
     * 连接超时时间（毫秒）。默认为 10 秒。
     */
    private long connectTimeout = 10000;

    /**
     * 写入超时时间（毫秒）。默认为 60 秒。
     */
    private long writeTimeout = 60000;

    /**
     * 读取超时时间（毫秒）。默认为 60 秒。
     */
    private long readTimeout = 60000;
}
