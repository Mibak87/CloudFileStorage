package main.cloudfilestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenameFileDto {
    private String userName;
    private String fileName;
    private String newFileName;
}
