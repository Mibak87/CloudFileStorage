package main.cloudfilestorage.service;

import io.minio.errors.MinioException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.FileDto;
import main.cloudfilestorage.dto.RenameFileDto;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.dto.ViewFilesDto;
import main.cloudfilestorage.exception.FailedDeletionException;
import main.cloudfilestorage.repository.MinioRepository;
import main.cloudfilestorage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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

    public void deleteFile(FileDto fileDto) throws FailedDeletionException {
        log.info("Удаляем файл " + fileDto.getFileName() + " у пользователя " + fileDto.getUserName() + " .");
        minioRepository.deleteFile(getFileFullName(fileDto.getUserName()
                , fileDto.getPath()
                , fileDto.getFileName()));
    }

    public void renameFile(RenameFileDto renameFileDto) {
        log.info("Переименование файла "+renameFileDto.getFileName()+" в файл "+renameFileDto.getNewFileName());
        if (renameFileDto.getFileName().endsWith("/")) {
            renameFileDto.setNewFileName(renameFileDto.getNewFileName() + "/");
        }
        minioRepository.renameFile(getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getPath()
                        ,renameFileDto.getFileName())
                        ,getFileFullName(renameFileDto.getUserName()
                        ,renameFileDto.getPath()
                        ,renameFileDto.getNewFileName()));
    }

    public void downloadFolder(HttpServletResponse response,FileDto fileDto) {
        log.info("Скачиваем папку " + fileDto.getFileName() + " у пользователя " + fileDto.getUserName() + " .");
        Set<String> filesToDownload = getAllFilesByDirectory(getFileFullName(fileDto.getUserName()
                ,fileDto.getPath()
                ,fileDto.getFileName()));
        log.info("В этой папке находятся файлы: " + filesToDownload);
        String userDirectory = getUserDirectory(fileDto.getUserName());
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (String file : filesToDownload) {
                if (!file.endsWith("/")) {
                    ZipEntry zipEntry = new ZipEntry(file.replace(userDirectory,""));
                    zipOut.putNextEntry(zipEntry);
                    InputStream is = minioRepository.downloadFile(file);
                    StreamUtils.copy(is, zipOut);
                    zipOut.closeEntry();
                }
            }
        } catch (Exception e) {
            log.error("При архивировании папки произошла ошибка.");
        }
    }

    public Resource downloadFile(FileDto fileDto) {
        log.info("Скачивание файла " + fileDto.getFileName());
        return new InputStreamResource(minioRepository.downloadFile(getFileFullName(fileDto.getUserName()
                        ,fileDto.getPath()
                        ,fileDto.getFileName())));
    }

    public ViewFilesDto getUserFiles(String userName, String path) {
        ViewFilesDto viewFilesDto = new ViewFilesDto();
        String userDirectory = getUserDirectory(userName);
        if (path == null) {
            path = "";
        }
        userDirectory = userDirectory + path;
        log.info("Получаем все файлы пользователя " + userName + " из папки " + userDirectory);
        List<String> allUserFiles = minioRepository.getFilesByDirectory(userDirectory);
        log.info("Вот они: " + allUserFiles);
        List<String> userFiles = new ArrayList<>();
        List<String> userDirectories = new ArrayList<>();
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

    public void deleteFolder(FileDto fileDto) throws FailedDeletionException {
        log.info("Удаляем папку " + fileDto.getFileName() + " у пользователя " + fileDto.getUserName() + " .");
        Set<String> filesToDelete = getAllFilesByDirectory(getFileFullName(fileDto.getUserName()
                                            ,fileDto.getPath()
                                            ,fileDto.getFileName()));
        log.info("В этой папке находятся файлы: " + filesToDelete);
        for (String file : filesToDelete) {
            minioRepository.deleteFile(file);
        }
    }

    public Map<String,String> getFoundFiles(String userName, String query) {
        String userDirectory = getUserDirectory(userName);
        Set<String> allUserFiles = getAllFilesByDirectory(userDirectory);
        Map<String,String> foundFiles = new HashMap<>();
        for (String file : allUserFiles) {
            if (file.endsWith("/" + query + "/")
                    || file.contains("/" + query + ".")
                    || file.contains("/" + query)) {
                String regex = query.contains(".")
                        ? Pattern.quote(query) : "\\b" + Pattern.quote(query) + "\\.[a-zA-Z0-9_-]+\\b";
                String fileName = file.replace(userDirectory,"");
                String filePath = fileName.replaceAll(regex,"");
                foundFiles.put(fileName,filePath.isEmpty() ? "/" : "/?path=" + filePath);
            }
        }
        log.info("При поиске файлов и папок с именем <" + query + "> найдены: " + foundFiles);
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

    public Set<String> getAllFilesByDirectory(String directory) {
        List<String> files = minioRepository.getFilesByDirectory(directory);
        Set<String> allFiles = new HashSet<>(files);
        for (String file : files) {
            if (file.endsWith("/") && !file.equals(directory)) {
                Set<String> filesInInternalDirectory = getAllFilesByDirectory(file);
                allFiles.addAll(filesInInternalDirectory);
            }
        }
        //log.info("В папке с именем <" + directory + "> найдены: " + allFiles);
        return allFiles;
    }
}
