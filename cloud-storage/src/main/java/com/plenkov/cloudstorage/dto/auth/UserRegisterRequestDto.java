package com.plenkov.cloudstorage.dto.auth;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequestDto {
    @NotBlank(message = "Заполните поле логин")
    @Size(min = 5, max = 20, message = "Логин должен быть в пределах от 5 до 20 символов")
    private String username;
    @NotBlank(message = "Заполните поле пароль")
    @Size(min = 5, max = 20, message = "Пароль должен быть в пределах от 5 до 20 символов")
    private String password;
}
