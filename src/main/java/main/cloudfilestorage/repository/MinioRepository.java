package main.cloudfilestorage.repository;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Repository
public class MinioRepository {

    private final MinioClient minioClient;

    public MinioRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String fileName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder().bucket("user-files").object(fileName)
                    .stream(inputStream, -1, 10485760).build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
