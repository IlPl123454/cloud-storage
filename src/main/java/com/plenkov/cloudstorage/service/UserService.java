package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public UserDto getUser(@AuthenticationPrincipal UserDetailsImpl user) {
        return new UserDto(user.getUsername());
    }
}
