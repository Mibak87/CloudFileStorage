package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.RegisterDto;
import main.cloudfilestorage.model.User;
import main.cloudfilestorage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class MainController {

    private final UserService userService;

    @Autowired
    public MainController(UserService userService) {
        this.userService = userService;
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
            log.info("Регистрация пользователя " + "<" + registerDto.getUserName() + ">");
            User user = new User(registerDto.getUserName(),registerDto.getPassword());
            userService.register(user);
            return "redirect:/login";
        //}
        //return "redirect:/registration";
    }

    @RequestMapping("/files")
    public String files() {
        return "files";
    }
}
