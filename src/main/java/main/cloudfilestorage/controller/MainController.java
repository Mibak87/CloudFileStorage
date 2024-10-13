package main.cloudfilestorage.controller;

import lombok.extern.slf4j.Slf4j;
import main.cloudfilestorage.dto.RegisterDto;
import main.cloudfilestorage.model.User;
import main.cloudfilestorage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @RequestMapping("/")
    public String files(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("query");
        return "files";
    }

    @PostMapping("/search")
    public String postSearch(@ModelAttribute("query") String query, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("query", query);
        return "redirect:/search";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String query, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("query", query);
        return "search";
    }
}
