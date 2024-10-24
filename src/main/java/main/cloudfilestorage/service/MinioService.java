package main.cloudfilestorage.service;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
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

    public void deleteFile(String fileName) {
        minioRepository.deleteFile(fileName);
    }

    public void renameFile(String fileName, String newName) {
        log.info("Переименование файла "+fileName+" в файл "+getFileFullName(fileName,newName));
        minioRepository.renameFile(fileName, getFileFullName(fileName,newName));
    }

    public List<String> getUserFiles(String userName, String param) {
        //if (param == null) {
            String userDirectory = getUserDirectory(userName);
            List<String> userFiles = minioRepository.getAllFilesByUser(userDirectory);
            return  userFiles;
        //}
    }

    private String getUserDirectory(String userName) {
        Long userId = userRepository.findByUsername(userName).getId();
        return "user-" + userId + "-files";
    }

    private String getFileFullName(String fileName, String newName) {
        int index = fileName.lastIndexOf("-");
        String userDirectory = fileName.substring(0,index + 1);
        return userDirectory + newName;
    }
}
