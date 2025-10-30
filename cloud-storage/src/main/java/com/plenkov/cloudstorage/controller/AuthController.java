package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.register.UserRegisterRequestDto;
import com.plenkov.cloudstorage.repository.UserRepository;
import com.plenkov.cloudstorage.service.RegisterService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final RegisterService registerService;

    public AuthController(RegisterService registerService) {
        this.registerService = registerService;
    }


    @GetMapping("/login")
    public String login() {
        return "redirect:/login.html";
    }

    @GetMapping("/register")
    public String getRegister() {
        return "redirect:/register.html";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username, @RequestParam String password) {
        UserRegisterRequestDto user = new UserRegisterRequestDto(username, password);

        registerService.register(user);
        System.out.println(username + " зарегистрирован");

        return "redirect:/hello.html";
    }
}
