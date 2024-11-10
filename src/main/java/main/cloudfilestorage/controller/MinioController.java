package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.FileDto;
import main.cloudfilestorage.dto.RenameFileDto;
import main.cloudfilestorage.dto.UploadFileDto;
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

@Slf4j
@Controller
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
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
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteFileFromMinio(@RequestParam String fileToDelete,@RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(fileToDelete)
                .path(path)
                .build();
        minioService.deleteFile(fileDto);
        return "redirect:/";
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam String newFileName, @RequestParam("path") String fileName
            ,@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .userName(userName)
                .fileName(fileName)
                .path(path)
                .newFileName(newFileName)
                .build();
        minioService.renameFile(renameFileDto);
        return "redirect:/";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName,@RequestParam("path") String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        FileDto fileDto = FileDto.builder()
                .userName(userName)
                .fileName(fileName)
                .path(path)
                .build();
        Resource file = minioService.downloadFile(fileDto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(file);
    }

    @PostMapping("/createfolder")
    public String createFolder(@RequestParam String folderName,@RequestParam("path") String path) {
        log.info("Хотим создать папку внутри папки " + path);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        if (!folderName.isEmpty()) {
            minioService.createFolder(folderName,path,userName);
        }
        return "redirect:/";
    }
}
