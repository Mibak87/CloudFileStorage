package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.RegisterDto;
import main.cloudfilestorage.dto.ViewFilesDto;
import main.cloudfilestorage.exception.InvalidUrlException;
import main.cloudfilestorage.exception.NonUniqueUserNameException;
import main.cloudfilestorage.model.User;
import main.cloudfilestorage.service.StorageService;
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
    private final StorageService storageService;

    @Autowired
    public MainController(UserService userService, StorageService storageService) {
        this.userService = userService;
        this.storageService = storageService;
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("error", "Неправильные логин или пароль!");
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
    public String processRegister(@ModelAttribute("userRegister") RegisterDto registerDto, Model model) {
        if (registerDto.getUserName().isEmpty()
                || registerDto.getPassword().isEmpty()
                || registerDto.getConfirmPassword().isEmpty()) {
            model.addAttribute("error", "Поля не должны быть пустыми!");
            return "registration";
        }
        if (registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            log.info("Регистрация пользователя <{}>",registerDto.getUserName());
            User user = new User(registerDto.getUserName(),registerDto.getPassword());
            try {
                userService.register(user);
                log.info("Пользователь <{}> зарегистрирован.",registerDto.getUserName());
            } catch (NonUniqueUserNameException e) {
                log.error(e.toString());
                model.addAttribute("userNameError", "Пользователь с таким именем уже существует!");
                return "registration";
            }
            return "redirect:/login";
       }
        log.error("Пароли не совпадают!");
        model.addAttribute("confirmPasswordError", "Пароли не совпадают!");
        return "registration";
    }

    @GetMapping("/")
    public String files(@RequestParam(required = false) String path, Model model) {
        log.info("Выбрана папка: {}",path);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("folder",path);
        try {
            ViewFilesDto viewFilesDto = storageService.getUserFiles(username, path);
            model.addAttribute("viewFilesDto", viewFilesDto);
            return "files";
        } catch (InvalidUrlException e) {
            model.addAttribute("error","Папки по этому пути не существует!");
            log.error("Папки по пути {} не существует!",path);
            return "error";
        }
    }

    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        Map<String,String> foundFiles = storageService.getFoundFiles(username,query);
        model.addAttribute("foundFiles",foundFiles);
        return "search";
    }
}
