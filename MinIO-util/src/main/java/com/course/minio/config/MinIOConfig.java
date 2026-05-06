package com.course.minio.config;

import com.course.minio.properties.MinIOConfigProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}