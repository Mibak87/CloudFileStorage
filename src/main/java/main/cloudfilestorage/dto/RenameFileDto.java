package main.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RenameFileDto {
    private String userName;
    private String fileName;
    private String path;
    private String newFileName;
}
