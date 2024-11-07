package main.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@Builder
public class UploadFileDto {
    private String userName;
    private String fileName;
    private String path;
    private MultipartFile multipartFile;
}
