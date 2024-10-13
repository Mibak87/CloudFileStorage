package main.cloudfilestorage.config;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MinioConfiguration {
    public MinioClient minioClient() {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000")
                        .credentials("minioadmin", "minioadmin")
                        .build();
        try {
            minioClient.makeBucket(
                    MakeBucketArgs
                            .builder()
                            .bucket("user1")
                            .build());
        } catch (Exception e) {
            log.error("Не удалось создать корзину!");
        }
        return minioClient;
    }
}
