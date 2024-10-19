package main.cloudfilestorage.service;

import main.cloudfilestorage.repository.MinioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioService {
    private final MinioRepository minioRepository;

    @Autowired
    public MinioService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public void uploadFile(String fileName, MultipartFile file) {
        minioRepository.uploadFile(fileName,file);
    }
}
