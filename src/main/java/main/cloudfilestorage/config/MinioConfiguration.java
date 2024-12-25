package main.cloudfilestorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfiguration {
    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000")
                        .credentials("qU76th-3Uh", "yuThGF65tg")
                        .build();
        try {
            boolean found =
                    minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket("user-files")
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
