package main.cloudfilestorage.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ViewFilesDto {
    private List<String> pathList;
    private Map<String,String> linkMap;
    private String path;
    private List<String> directories;
    private Map<String,String> allPath;
    private List<String> files;
}
