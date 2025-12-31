package com.plenkov.cloudstorage.controller.api;

import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth Controller", description = "Контроллер для регистрации и аутентификации пользователя")
public interface AuthApi {
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя в системе. Логин должен быть уникальным."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(schema = @Schema(implementation = UserDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации"
    )
    @ApiResponse(
            responseCode = "409",
            description = "Пользователь с таким логином уже существует"
    )
    public UserDto doRegister(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto);


    @Operation(
            summary = "Вход в систему",
            description = "Проверяет учетные данные и создает сессию (JSESSIONID)"
    )
    @ApiResponse(responseCode = "200", description = "Успешная авторизация")
    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    public UserDto doSignIn(@Valid @RequestBody UserSignInRequestDto dto,
                            HttpServletRequest request,
                            HttpServletResponse response);
}
