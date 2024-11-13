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

import java.util.*;
import java.util.stream.Collectors;


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
        if (path == null) {
            path = "";
        }
        userDirectory = userDirectory + path;
        log.info("Получаем все файлы пользователя " + userName + " из папки " + userDirectory);
        List<String> allUserFiles = minioRepository.getAllFilesByUser(userDirectory);
        log.info("Вот они: " + allUserFiles);
        List<String> userFiles = new ArrayList<>();
        List<String> userDirectories = new ArrayList<>();
        Map<String,String> linkMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        String[] paths = path.split("/");
        StringBuilder linkPath = new StringBuilder("");
        for (String singlePath : paths) {
            pathList.add(singlePath + "/");
            linkPath.append(singlePath + "/");
            linkMap.put(singlePath + "/",linkPath.toString());
        }
        viewFilesDto.setPathList(pathList);
        viewFilesDto.setPath(path);
        viewFilesDto.setLinkMap(linkMap);
        for (String userFile : allUserFiles) {
            if (userFile.equals(userDirectory)) {
                continue;
            }
            String files = userFile.split(userDirectory)[1];
            if (files.endsWith("/")) {
                userDirectories.add(files);
                continue;
            }
            userFiles.add(files);
        }
        viewFilesDto.setFiles(userFiles);
        viewFilesDto.setDirectories(userDirectories);
        return viewFilesDto;
    }

    public void createFolder(String folderName,String path,String userName) {
        minioRepository.createFolder(getFileFullName(userName,path,folderName));
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
