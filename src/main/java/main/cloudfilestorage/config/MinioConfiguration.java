package main.cloudfilestorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfiguration {

    @Value ("${MINIO_ACCESS}")
    private String minioAccess;
    @Value ("${MINIO_SECRET}")
    private String minioSecret;
    @Value ("${MINIO_BUCKET}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000")
                        .credentials(minioAccess, minioSecret)
                        .build();
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucketName)
                                .build());
            } else {
                log.info("Такая корзина уже есть!");
            }
        } catch (Exception e) {
            log.error("Не удалось создать корзину!");
        }
        return minioClient;
    }
}
