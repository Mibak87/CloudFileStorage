package main.cloudfilestorage.repository;

import io.minio.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.exception.CreateFolderException;
import main.cloudfilestorage.exception.DeleteFileException;
import main.cloudfilestorage.exception.DownloadFileException;
import main.cloudfilestorage.exception.RenameFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class MinioRepository {

    @Value("${MINIO_BUCKET}")
    private String bucketName;
    private final MinioClient minioClient;

    public MinioRepository(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String fileName, MultipartFile file) {
        log.info("Загружаем файл: {}",fileName);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, -1, 10485760)
                    .build());
            log.info("Файл " + fileName + " успешно загружен.");
        } catch (Exception e) {
            log.error("Не удалось загрузить файл: {}",fileName);
        }
    }

    public List<String> getFilesByDirectory(String userDirectory) {
        List<String> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(userDirectory)
                            .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                files.add(item.objectName());
            }
        } catch (Exception e) {
            log.error("Не удалось получить файлы из папки: {}",userDirectory);
        }
        return files;
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            log.info("Файл <{}> удален.",fileName);
        } catch (Exception e) {
            log.error("Не удалось удалить файл: {}",fileName);
            throw new DeleteFileException("Не удалось удалить файл: " + fileName);
        }
    }

    public void renameFile(String fileName, String newName) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(newName)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(fileName)
                                            .build())
                            .build());
            deleteFile(fileName);
        } catch (Exception e) {
            log.error("Не удалось переименовать файл: {}",fileName);
            throw new RenameFileException("Не удалось переименовать файл: " + fileName);
        }
    }

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("Не удалось скачать файл: {}",fileName);
            throw new DownloadFileException("Не удалось скачать файл: " + fileName);
        }
    }

    public void createFolder(String folderName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderName + "/")
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build()
            );
            log.info("Создали папку с полным названием: {}",folderName);
        } catch (Exception e) {
            log.error("Не удалось создать папку: {}",folderName);
            throw new CreateFolderException("Не удалось создать папку: " + folderName);
        }
    }
}
