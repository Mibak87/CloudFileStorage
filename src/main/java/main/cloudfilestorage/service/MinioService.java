package main.cloudfilestorage.service;

import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MinioService {
    private final MinioRepository minioRepository;
    private final UserRepository userRepository;

    @Autowired
    public MinioService(MinioRepository minioRepository, UserRepository userRepository) {
        this.minioRepository = minioRepository;
        this.userRepository = userRepository;
    }

    public void uploadFile(UploadFileDto uploadFileDto) {
        String userDirectory = getUserDirectory(uploadFileDto.getUserName());
        String fileName = userDirectory + "-" + uploadFileDto.getFileName();
        minioRepository.uploadFile(fileName,uploadFileDto.getMultipartFile());
    }

    public List<String> getUserFiles(String userName, String param) {
        //if (param == null) {
            String userDirectory = getUserDirectory(userName);
            List<String> userFiles = minioRepository.getAllFilesByUser(userDirectory);
            return  userFiles;
        //}
    }

    public String getUserDirectory(String userName) {
        Long userId = userRepository.findByUsername(userName).getId();
        return "user-" + userId + "-files";
    }
}
