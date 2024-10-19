package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.service.MinioService;
import org.springframework.stereotype.Controller;
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
            String fileName = file.getOriginalFilename();
            minioService.uploadFile(fileName, file);
        } catch (Exception e) {
            log.error("Загрузка не удалась.");
            e.printStackTrace();
        }
        return "redirect:/";
    }
}
