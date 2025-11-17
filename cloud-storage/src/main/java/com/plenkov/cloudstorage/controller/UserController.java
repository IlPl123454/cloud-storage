package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserDto getUser(@AuthenticationPrincipal UserDto userDto) {
        return userService.getUser(userDto);
    }
}