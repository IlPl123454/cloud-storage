package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterResponseDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Tag(name = "Название контроллера", description = "Описание контроллера")
    @PostMapping("/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserRegisterResponseDto doRegister(@Valid @RequestBody UserRegisterRequestDto dto) {
        return authService.register(dto);
    }

    @PostMapping("/sign-in")
    public UserDto doSignIn(@Valid @RequestBody UserSignInRequestDto dto,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        return authService.authenticate(dto, request, response);
    }
}
