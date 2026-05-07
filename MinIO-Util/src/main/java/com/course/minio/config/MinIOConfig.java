package com.course.minio.config;

import com.course.minio.properties.MinIOConfigProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
@EnableConfigurationProperties(MinIOConfigProperties.class)
public class MinIOConfig {

    @Bean
    public MinioClient minioClient(MinIOConfigProperties minIOConfigProperties) {
        log.info("带端点初始化 MinIO 客户端: {}", minIOConfigProperties.getEndpoint());
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minIOConfigProperties.getEndpoint())
                    .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                    .build();

            // 设置超时时间
            minioClient.setTimeout(
                    minIOConfigProperties.getConnectTimeout(),
                    minIOConfigProperties.getWriteTimeout(),
                    minIOConfigProperties.getReadTimeout()
            );

            return minioClient;
        } catch (Exception e) {
            log.error("未能初始化 MinIO 客户端", e);
            throw new RuntimeException("未能初始化 MinIO 客户端", e);
        }
    }

    /**
     * AWS S3Client —— 用于分片上传的核心操作
     * MinIO 要求必须开启 pathStyleAccess（路径风格访问）
     * Region 填什么无所谓，MinIO 不校验
     */
    @Bean
    public S3Client s3Client(MinIOConfigProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)  // MinIO 必须开启
                        .build())
                .build();
    }

    /**
     * S3Presigner —— 用于生成分片预签名 PUT URL
     * 客户端拿到 URL 后直传 MinIO，不经过服务端带宽
     */
    @Bean
    public S3Presigner s3Presigner(MinIOConfigProperties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}