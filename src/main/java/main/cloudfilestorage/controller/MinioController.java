package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
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
    public String uploadFileToMinIO(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Пытаемся загрузить на обменник файл.");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String fileName = file.getOriginalFilename();
            UploadFileDto uploadFileDto = new UploadFileDto(username,fileName,file);
            minioService.uploadFile(uploadFileDto);
        } catch (Exception e) {
            log.error("Загрузка не удалась.");
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String deleteFileFromMinio(@RequestParam String fileToDelete) {
        minioService.deleteFile(fileToDelete);
        return "redirect:/";
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam String newFileName, @RequestParam String fileName) {
        minioService.renameFile(fileName,newFileName);
        return "redirect:/";
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        Resource file = minioService.downloadFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(file);
    }
}
