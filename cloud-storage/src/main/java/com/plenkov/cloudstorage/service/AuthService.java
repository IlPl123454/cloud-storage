package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterResponseDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInResponseDto;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import com.plenkov.cloudstorage.model.User;
import com.plenkov.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {

        Optional<User> userByLogin = userRepository.findByLogin(userRegisterRequestDto.getUsername());
        if (userByLogin.isPresent()) {
            throw new UserAlreadyExistException(userRegisterRequestDto.getUsername());
        }

        User user = new User(
                null,
                userRegisterRequestDto.getUsername(),
                passwordEncoder.encode(userRegisterRequestDto.getPassword()));

        userRepository.save(user);

        return new UserRegisterResponseDto(user.getLogin());
    }

    public UserSignInResponseDto authenticate(UserSignInRequestDto userSignInRequestDto) {
        return new UserSignInResponseDto(userSignInRequestDto.username());
    }
}
