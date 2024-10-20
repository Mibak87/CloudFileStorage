package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.UploadFileDto;
import main.cloudfilestorage.service.MinioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String uploadFileToMinIO(@RequestParam("file") MultipartFile file, Model model) {
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
}
