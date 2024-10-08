package main.cloudfilestorage.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {

    private String userName;
    private String password;
    private String confirmPassword;
}
