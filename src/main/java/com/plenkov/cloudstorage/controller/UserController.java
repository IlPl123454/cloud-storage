package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.controller.api.UserApi;
import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController implements UserApi {
    private final UserService userService;

    @Override
    @GetMapping("/me")
    public UserDto getUser(@AuthenticationPrincipal UserDetailsImpl user) {
        return userService.getUser(user);
    }
}