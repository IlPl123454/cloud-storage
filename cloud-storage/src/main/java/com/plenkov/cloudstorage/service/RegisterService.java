package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.register.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.register.UserRegisterResponseDto;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import com.plenkov.cloudstorage.model.User;
import com.plenkov.cloudstorage.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Service
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserRegisterResponseDto register(UserRegisterRequestDto userRegisterRequestDto) {

        Optional<User> userOpt = userRepository.findByLogin(userRegisterRequestDto.getUsername());
        if (userOpt.isPresent()) {
            throw new UserAlreadyExistException(userRegisterRequestDto.getUsername());
        }

        User user = new User();
        user.setLogin(userRegisterRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRegisterRequestDto.getPassword()));

        userRepository.save(user);

        return new UserRegisterResponseDto(user.getLogin());
    }
}
