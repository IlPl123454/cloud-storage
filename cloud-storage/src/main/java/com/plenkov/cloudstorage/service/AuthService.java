package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterResponseDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInResponseDto;
import com.plenkov.cloudstorage.exception.AuthException;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import com.plenkov.cloudstorage.model.User;
import com.plenkov.cloudstorage.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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

    public UserSignInResponseDto authenticate(UserSignInRequestDto dto,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        dto.username(),
                        dto.password()
                );

        Authentication auth;

        try {
            auth = authenticationManager.authenticate(token);

        } catch (Exception exception) {
            throw new AuthException("Authentication failed");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        request.getSession(true);

        SecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        return new UserSignInResponseDto(dto.username());
    }
}
