package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.exception.NotAuthorized;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public UserDto getUser(@AuthenticationPrincipal UserDetailsImpl user) {
        if (user == null) {
            throw new NotAuthorized("Пользователь не авторизован");
        }
        return new UserDto(user.getUsername());
    }
}
