package main.cloudfilestorage.controller;

import main.cloudfilestorage.model.User;
import main.cloudfilestorage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String userName, @RequestParam String password) {

        return "redirect:/files";
    }

    @RequestMapping("/registration")
    public String register() {
        return "registration";
    }

    @PostMapping("/registration")
    public String processLogin(@RequestParam String userName, @RequestParam String password,
                               @RequestParam String passwordRepeat) {
        if (password.equals(passwordRepeat)) {
            User user = new User(userName,password);
            userService.register(user);
            return "redirect:/files";
        }
        return "redirect:/registration";
    }

    @RequestMapping("/files")
    public String files() {
        return "files";
    }
}
