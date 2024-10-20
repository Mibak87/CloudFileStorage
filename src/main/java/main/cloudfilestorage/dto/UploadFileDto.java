package main.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class UploadFileDto {
    private String userName;
    private String fileName;
    private MultipartFile multipartFile;
}
