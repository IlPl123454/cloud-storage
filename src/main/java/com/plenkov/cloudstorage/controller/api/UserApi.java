package com.plenkov.cloudstorage.controller.api;

import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User Controller", description = "Управление данными профиля пользователя")
public interface UserApi {

    @Operation(
            summary = "Получить информацию о себе",
            description = "Возвращает данные текущего авторизованного пользователя на основе сессии"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Данные профиля успешно получены",
            content = @Content(schema = @Schema(implementation = UserDto.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован"
    )
    UserDto getUser(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user);
}