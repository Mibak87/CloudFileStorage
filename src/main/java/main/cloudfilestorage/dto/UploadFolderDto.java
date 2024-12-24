package main.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UploadFolderDto {
    private String userName;
    private String path;
    private List<MultipartFile> multipartFiles;
}
