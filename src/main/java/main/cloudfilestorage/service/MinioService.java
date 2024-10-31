package main.cloudfilestorage.service;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.RenameFileDto;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
        minioRepository.uploadFile(getFileFullName(uploadFileDto.getUserName(),uploadFileDto.getFileName())
                ,uploadFileDto.getMultipartFile());
    }

    public void deleteFile(String fileName,String userName) {
        log.info("Удаляем файл " + fileName + " у пользователя " + userName + " .");
        minioRepository.deleteFile(getFileFullName(userName,fileName));
    }

    public void renameFile(RenameFileDto renameFileDto) {
        log.info("Переименование файла "+renameFileDto.getFileName()+" в файл "+renameFileDto.getNewFileName());
        minioRepository.renameFile(getFileFullName(renameFileDto.getUserName(),renameFileDto.getFileName())
                ,getFileFullName(renameFileDto.getUserName(),renameFileDto.getNewFileName()));
    }

    public Resource downloadFile(String fileName,String userName) {
        log.info("Скачивание файла " + fileName);
        return minioRepository.downloadFile(getFileFullName(userName,fileName),fileName);
    }

    public List<String> getUserFiles(String userName, String param) {
        //if (param == null) {
            String userDirectory = getUserDirectory(userName);
            List<String> userFiles = minioRepository.getAllFilesByUser(userDirectory);
            return  userFiles;
        //}
    }

    public void createFolder(String folderName,String userName) {
        minioRepository.createFolder(getUserDirectory(userName) + folderName);
    }

    private String getUserDirectory(String userName) {
        Long userId = userRepository.findByUsername(userName).getId();
        return "user-" + userId + "-files/";
    }

    private String getFileFullName(String userName, String fileName) {
        return getUserDirectory(userName) + fileName;
    }

    private String getFileShortName(String fileName) {
        int index = fileName.lastIndexOf("/");
        return "D:/Downloads/" + fileName.substring(index + 1);
    }
}
