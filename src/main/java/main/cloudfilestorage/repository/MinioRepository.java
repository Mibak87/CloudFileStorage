package main.cloudfilestorage.repository;

import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class MinioRepository {

    private final MinioClient minioClient;

    public MinioRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String fileName, MultipartFile file) {
        log.info("Загружаем файл: " + fileName);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("user-files")
                    .object(fileName)
                    .stream(inputStream, -1, 10485760)
                    .build());
            log.info("Файл " + fileName + " успешно загружен.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilesByDirectory(String userDirectory) {
        List<String> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket("user-files")
                            .prefix(userDirectory)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renameFile(String fileName, String newName) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket("user-files")
                            .object(newName)
                            .source(
                                    CopySource.builder()
                                            .bucket("user-files")
                                            .object(fileName)
                                            .build())
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        deleteFile(fileName);
    }

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("user-files")
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createFolder(String folderName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object(folderName + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
            log.info("Создали папку с полным названием: " + folderName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
