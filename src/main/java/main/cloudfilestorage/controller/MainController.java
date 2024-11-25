package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.RegisterDto;
import main.cloudfilestorage.dto.ViewFilesDto;
import main.cloudfilestorage.model.User;
import main.cloudfilestorage.service.MinioService;
import main.cloudfilestorage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
public class MainController {

    private final UserService userService;
    private final MinioService minioService;

    @Autowired
    public MainController(UserService userService, MinioService minioService) {
        this.userService = userService;
        this.minioService = minioService;
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());
        log.info("Зашли в авторизацию.");
        return "login";
    }

    @RequestMapping("/registration")
    public String register(Model model) {
        model.addAttribute("userRegister", new RegisterDto());
        log.info("Зашли в регистрацию.");
        return "registration";
    }

    @PostMapping("/registration")
    public String processRegister(@ModelAttribute("userRegister") RegisterDto registerDto) {
        //if (registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            log.info("Регистрация пользователя <" + registerDto.getUserName() + ">");
            User user = new User(registerDto.getUserName(),registerDto.getPassword());
            userService.register(user);
            log.info("Пользователь <" + registerDto.getUserName() + "> зарегистрирован.");
            return "redirect:/login";
       //}
        //return "redirect:/registration";
    }

    @GetMapping("/")
    public String files(@RequestParam(required = false) String path, Model model) {
        log.info("Выбрана папка: " + path);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("folder",path);
        ViewFilesDto viewFilesDto = minioService.getUserFiles(username,path);
        model.addAttribute("viewFilesDto", viewFilesDto);
        return "files";
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        Map<String,String> foundFiles = minioService.getFoundFiles(username,query);
        model.addAttribute("foundFiles",foundFiles);
        return "search";
    }
}
