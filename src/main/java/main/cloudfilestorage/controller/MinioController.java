package main.cloudfilestorage.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.FileDto;
import main.cloudfilestorage.dto.RenameFileDto;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.exception.CreateFolderException;
import main.cloudfilestorage.exception.DeleteFileException;
import main.cloudfilestorage.exception.DownloadFileException;
import main.cloudfilestorage.exception.RenameFileException;
import main.cloudfilestorage.service.MinioService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload/file")
    public String uploadFileToMinIO(@RequestParam("file") MultipartFile file,@RequestParam("path") String path) {
        try {
            log.info("Пытаемся загрузить на обменник файл в папку " + path);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            String fileName = file.getOriginalFilename();
            UploadFileDto uploadFileDto = UploadFileDto.builder()
                    .userName(userName)
                    .fileName(fileName)
                    .path(path)
                    .multipartFile(file)
                    .build();
            minioService.uploadFile(uploadFileDto);
        } catch (Exception e) {
            log.error("Загрузка не удалась.");
            e.printStackTrace();
        }
        return getURL(path);
    }

    @PostMapping("/upload/folder")
    public String uploadFolderToMinIO(@RequestParam("folder") List<MultipartFile> files, @RequestParam("path") String path) {
        try {
            log.info("Пытаемся загрузить на обменник папку в папку " + path);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            for (MultipartFile file : files) {
                String fileName = file.getOriginalFilename();
                UploadFileDto uploadFileDto = UploadFileDto.builder()
                        .userName(userName)
                        .fileName(fileName)
                        .path(path)
                        .multipartFile(file)
                        .build();
                minioService.uploadFile(uploadFileDto);
            }
        } catch (Exception e) {
            log.error("Загрузка не удалась.");
            e.printStackTrace();
        }
        return getURL(path);
    }

    @PostMapping("/delete/file")
    public String deleteFileFromMinio(@RequestParam String fileToDelete,@RequestParam("path") String path,
             RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(fileToDelete)
                .path(path)
                .build();
        try {
            minioService.deleteFile(fileDto);
        } catch (DeleteFileException e) {
            redirectAttributes.addFlashAttribute("error", "При удалении файла произошла ошибка!");
        }
        return getURL(path);
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam String newFileName, @RequestParam String fileName,
            @RequestParam("path") String path, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .userName(userName)
                .fileName(fileName)
                .path(path)
                .newFileName(newFileName)
                .build();
        try {
            minioService.renameFile(renameFileDto);
        } catch (RenameFileException e) {
            redirectAttributes.addFlashAttribute("error", "При переименовании файла (или папки) произошла ошибка!");
        }
        return getURL(path);
    }

    @GetMapping("/download/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName,@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(fileName)
                .path(path)
                .build();
        try {
            Resource file = minioService.downloadFile(fileDto);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(file);
        } catch (DownloadFileException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/download/folder")
    public String downloadFolder(HttpServletResponse response, @RequestParam String fileName, @RequestParam String path,
                                 RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(fileName)
                .path(path)
                .build();
        try {
            String outputName = fileName.replace("/", "") + ".zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + outputName);
            minioService.downloadFolder(response, fileDto);
            return getURL(path);
        } catch (DownloadFileException e) {
            redirectAttributes.addFlashAttribute("error", "Не удалось скачать папку!");
            return getURL(path);
        }
    }

    @PostMapping("/create/folder")
    public String createFolder(@RequestParam String folderName,@RequestParam("path") String path,
                               RedirectAttributes redirectAttributes) {
        log.info("Хотим создать папку " + folderName + " внутри папки " + path);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if (folderName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Название папки не должно быть пустым!");
            return getURL(path);
        }
        try {
            minioService.createFolder(folderName, path, userName);
        } catch (CreateFolderException e) {
            redirectAttributes.addFlashAttribute("error", "При создании папки произошла ошибка!");
        }
        return getURL(path);
    }

    @PostMapping("/delete/folder")
    public String deleteFolderFromMinio(@RequestParam String folderToDelete,@RequestParam("path") String path,
                                        RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(folderToDelete)
                .path(path)
                .build();
        try {
            minioService.deleteFolder(fileDto);
        } catch (DeleteFileException e) {
            redirectAttributes.addFlashAttribute("error", "При удалении папки произошла ошибка!");
        }
        return getURL(path);
    }

    private String getURL(String path) {
        return (path.isEmpty()) ? "redirect:/" : ("redirect:/?path=" + path);
    }
}
