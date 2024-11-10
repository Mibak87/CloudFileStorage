package main.cloudfilestorage.dto;

import lombok.Data;

import java.util.List;

@Data
public class ViewFilesDto {
    //private List<String> path;
    private String path;
    private List<String> directories;
    private List<String> files;
}
