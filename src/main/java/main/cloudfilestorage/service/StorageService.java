package main.cloudfilestorage.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.*;
import main.cloudfilestorage.exception.*;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class StorageService {
    private final MinioRepository minioRepository;
    private final UserRepository userRepository;

    @Autowired
    public StorageService(MinioRepository minioRepository, UserRepository userRepository) {
        this.minioRepository = minioRepository;
        this.userRepository = userRepository;
    }

    public void uploadFile(UploadFileDto uploadFileDto) {
        minioRepository.uploadFile(getFileFullName(uploadFileDto.getUserName()
                ,uploadFileDto.getPath()
                ,uploadFileDto.getFileName())
                ,uploadFileDto.getMultipartFile());
    }

    public void uploadFolder(UploadFolderDto uploadFolderDto) {
        for (MultipartFile file : uploadFolderDto.getMultipartFiles()) {
            String fileName = file.getOriginalFilename();
            UploadFileDto uploadFileDto = UploadFileDto.builder()
                    .userName(uploadFolderDto.getUserName())
                    .fileName(fileName)
                    .path(uploadFolderDto.getPath())
                    .multipartFile(file)
                    .build();
            uploadFile(uploadFileDto);
        }
    }

    public void deleteFile(FileDto fileDto) {
        log.info("Удаляем файл {} у пользователя {}.",fileDto.getFileName(),fileDto.getUserName());
        minioRepository.deleteFile(getFileFullName(fileDto.getUserName()
                , fileDto.getPath()
                , fileDto.getFileName()));
    }

    public void renameFile(RenameFileDto renameFileDto) {
        log.info("Переименование файла {} в файл {}.",renameFileDto.getFileName(),renameFileDto.getNewFileName());
        if (renameFileDto.getFileName().endsWith("/")) {
            renameFileDto.setNewFileName(renameFileDto.getNewFileName() + "/");
        }
        String folderPath = getFileFullName(renameFileDto.getUserName(),renameFileDto.getPath(),"");
        List<String> filesInFolder = minioRepository.getFilesByDirectory(folderPath);
        for (String file : filesInFolder) {
            if (file.replace(folderPath,"").equals(renameFileDto.getNewFileName())) {
                throw new NonUniqueFileNameException("Файл(папка) с таким именем уже существует в этой папке!");
            }
        }
        minioRepository.renameFile(getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getPath()
                        ,renameFileDto.getFileName())
                        ,getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getPath()
                        ,renameFileDto.getNewFileName()));
    }

    public void downloadFolder(HttpServletResponse response,FileDto fileDto) {
        log.info("Скачиваем папку {} у пользователя {}.",fileDto.getFileName(),fileDto.getUserName());
        Set<String> filesToDownload = getAllFilesByDirectory(getFileFullName(fileDto.getUserName()
                ,fileDto.getPath()
                ,fileDto.getFileName()));
        log.info("В этой папке находятся файлы: {}",filesToDownload);
        String folderPath = getUserDirectory(fileDto.getUserName()) + fileDto.getPath();
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (String file : filesToDownload) {
                if (!file.endsWith("/")) {
                    ZipEntry zipEntry = new ZipEntry(file.replace(folderPath,""));
                    zipOut.putNextEntry(zipEntry);
                    InputStream is = minioRepository.downloadFile(file);
                    StreamUtils.copy(is, zipOut);
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            log.error("При архивировании папки произошла ошибка.");
            throw new DownloadFileException("При архивировании и скачивании папки произошла ошибка.");
        }
    }

    public Resource downloadFile(FileDto fileDto) {
        log.info("Скачивание файла {}",fileDto.getFileName());
        return new InputStreamResource(minioRepository.downloadFile(getFileFullName(fileDto.getUserName()
                        ,fileDto.getPath()
                        ,fileDto.getFileName())));
    }

    public ViewFilesDto getUserFiles(String userName, String path) {
        String userDirectory = getUserDirectory(userName);
        if (path == null) {
            path = "";
        }
        userDirectory = userDirectory + path;
        log.info("Получаем все файлы пользователя {} из папки {}.",userName,userDirectory);
        List<String> allUserFiles = minioRepository.getFilesByDirectory(userDirectory);
        log.info("Вот они: " + allUserFiles);
        if (allUserFiles.isEmpty() && !path.isEmpty()) {
            throw new InvalidUrlException("Папки по этому пути не существует!");
        }
        List<String> userFiles = new ArrayList<>();
        List<String> userDirectories = new ArrayList<>();
        for (String userFile : allUserFiles) {
            if (userFile.equals(userDirectory)) {
                continue;
            }
            String files = userFile.replace(userDirectory,"");
            if (files.endsWith("/")) {
                userDirectories.add(files);
                continue;
            }
            userFiles.add(files);
        }
        ViewFilesDto viewFilesDto = getViewFilesDto(path);
        viewFilesDto.setFiles(userFiles);
        viewFilesDto.setDirectories(userDirectories);
        return viewFilesDto;
    }

    private ViewFilesDto getViewFilesDto(String path) {
        Map<String,String> linkMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();
        String[] paths = ("/" + path).split("/");
        if (paths.length == 0) {
            pathList.add("/");
        }
        StringBuilder linkPath = new StringBuilder("/?path=");
        for (String singlePath : paths) {
            pathList.add(singlePath + "/");
            if (singlePath.isEmpty()) {
                linkMap.put(singlePath + "/","/");
                continue;
            }
            linkMap.put(singlePath + "/", linkPath.append(singlePath).append("/").toString());
        }
        return ViewFilesDto.builder()
                .pathList(pathList)
                .path(path)
                .linkMap(linkMap)
                .build();
    }

    public void createFolder(String folderName,String path,String userName) {
        minioRepository.createFolder(getFileFullName(userName,path,folderName));
    }

    public void deleteFolder(FileDto fileDto) throws DeleteFileException {
        log.info("Удаляем папку {} у пользователя {}.",fileDto.getFileName(),fileDto.getUserName());
        Set<String> filesToDelete = getAllFilesByDirectory(getFileFullName(fileDto.getUserName()
                                            ,fileDto.getPath()
                                            ,fileDto.getFileName()));
        log.info("В этой папке находятся файлы: {}",filesToDelete);
        for (String file : filesToDelete) {
            minioRepository.deleteFile(file);
        }
    }

    public Map<String,String> getFoundFiles(String userName, String query) {
        String userDirectory = getUserDirectory(userName);
        Set<String> allUserFiles = getAllFilesByDirectory(userDirectory);
        Map<String,String> foundFiles = new HashMap<>();
        for (String file : allUserFiles) {
            String[] files = file.split("/");
            String filePath = file.substring(0, file.lastIndexOf(files[files.length - 1]))
                    .replace(userDirectory, "");
            String fileLink = (filePath.isEmpty() || filePath.equals("/")) ? "/" : "/?path=" + filePath;
            if (query.split("\\.").length == 2 && query.equalsIgnoreCase(files[files.length-1])) {
                foundFiles.put(files[files.length-1],fileLink);
                continue;
            }
            String fileName = files[files.length-1].split("\\.")[0];
            if (fileName.isEmpty()) {
                fileName = files[files.length-2];
            }
            if (query.equalsIgnoreCase(fileName)) {
                foundFiles.put(files[files.length-1].isEmpty()
                        ? (files[files.length-2] + "/")
                        : files[files.length-1],fileLink);
            }
        }
        log.info("При поиске файлов и папок с именем <{}> найдены: {}.",query,foundFiles);
        return foundFiles;
    }

    private String getUserDirectory(String userName) {
        Long userId = userRepository.findByUsername(userName).orElseThrow().getId();
        return "user-" + userId + "-files/";
    }

    private String getFileFullName(String userName,String path,String fileName) {
        if (path == null) {
            return getUserDirectory(userName) + fileName;
        }
        return getUserDirectory(userName) + path + fileName;
    }

    private Set<String> getAllFilesByDirectory(String directory) {
        List<String> files = minioRepository.getFilesByDirectory(directory);
        Set<String> allFiles = new HashSet<>(files);
        for (String file : files) {
            if (file.endsWith("/") && !file.equals(directory)) {
                Set<String> filesInInternalDirectory = getAllFilesByDirectory(file);
                allFiles.addAll(filesInInternalDirectory);
            }
        }
        return allFiles;
    }
}
