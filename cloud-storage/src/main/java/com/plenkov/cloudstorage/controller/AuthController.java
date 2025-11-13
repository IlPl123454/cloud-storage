package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterResponseDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInResponseDto;
import com.plenkov.cloudstorage.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public UserRegisterResponseDto doRegister(@Valid @RequestBody UserRegisterRequestDto dto) {
        return authService.register(dto);
    }

    @PostMapping("/sign-in")
    public UserSignInResponseDto doSignIn(@Valid @RequestBody UserSignInRequestDto dto,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {

        return authService.authenticate(dto, request, response);
    }
}
