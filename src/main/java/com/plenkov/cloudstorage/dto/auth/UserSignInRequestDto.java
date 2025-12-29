package com.plenkov.cloudstorage.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignInRequestDto(
        @NotBlank(message = "Заполните поле логин")
        @Size(min = 5, max = 20, message = "Логин должен быть в пределах от 5 до 20 символов")
        String username,
        @NotBlank(message = "Заполните поле пароль")
        @Size(min = 5, max = 20, message = "Пароль должен быть в пределах от 5 до 20 символов")
        String password) {
}
