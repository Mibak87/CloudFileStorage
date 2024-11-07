package main.cloudfilestorage.service;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.FileDto;
import main.cloudfilestorage.dto.RenameFileDto;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.dto.ViewFilesDto;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        minioRepository.uploadFile(getFileFullName(uploadFileDto.getUserName()
                ,uploadFileDto.getPath()
                ,uploadFileDto.getFileName())
                ,uploadFileDto.getMultipartFile());
    }

    public void deleteFile(FileDto fileDto) {
        log.info("Удаляем файл " + fileDto.getFileName() + " у пользователя " + fileDto.getUserName() + " .");
        minioRepository.deleteFile(getFileFullName(fileDto.getUserName()
                ,fileDto.getPath()
                ,fileDto.getFileName()));
    }

    public void renameFile(RenameFileDto renameFileDto) {
        log.info("Переименование файла "+renameFileDto.getFileName()+" в файл "+renameFileDto.getNewFileName());
        minioRepository.renameFile(getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getFileName()
                        ,renameFileDto.getPath())
                        ,getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getPath()
                        ,renameFileDto.getNewFileName()));
    }

    public Resource downloadFile(FileDto fileDto) {
        log.info("Скачивание файла " + fileDto.getFileName());
        return minioRepository.downloadFile(getFileFullName(fileDto.getUserName()
                        ,fileDto.getPath()
                        ,fileDto.getFileName())
                        ,fileDto.getFileName());
    }

    public ViewFilesDto getUserFiles(String userName, String path) {
        ViewFilesDto viewFilesDto = new ViewFilesDto();
        String userDirectory = getUserDirectory(userName);
        List<String> allUserFiles = minioRepository.getAllFilesByUser(userDirectory);
        List<String> userFiles = new ArrayList<>();
        List<String> userDirectories = new ArrayList<>();
        List<String> userPath = new ArrayList<>();
        if (path == null) {
            viewFilesDto.setPath(userPath);
            for (String userFile : allUserFiles) {
                String[] files = userFile.split("/");
                if (files.length == 2 && !userFile.endsWith("/")) {
                    userFiles.add(files[1]);
                    continue;
                }
                userDirectories.add(files[1] + "/");
            }
            viewFilesDto.setFiles(userFiles);
            viewFilesDto.setDirectories(userDirectories);
            return  viewFilesDto;
        } else {
            for (String userFile : allUserFiles) {
                if (userFile.contains(path)) {
                    String[] files = userFile.split(path);
                    if (files.length == 2) {
                        userFiles.add(files[1]);
                        userPath.add(files[0] + path);
                    }
                }
            }
            viewFilesDto.setFiles(userFiles);
            return viewFilesDto;
        }
    }

    public void createFolder(String folderName,String userName) {
        minioRepository.createFolder(getUserDirectory(userName) + folderName);
    }

    private String getUserDirectory(String userName) {
        Long userId = userRepository.findByUsername(userName).getId();
        return "user-" + userId + "-files/";
    }

    private String getFileFullName(String userName,String path,String fileName) {
        if (path == null) {
            return getUserDirectory(userName) + fileName;
        }
        return getUserDirectory(userName) + path + fileName;
    }
}
