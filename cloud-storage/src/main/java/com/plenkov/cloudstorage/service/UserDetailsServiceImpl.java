package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.model.User;
import com.plenkov.cloudstorage.repository.UserRepository;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByLogin(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new UserDetailsImpl(user.get());
    }
}
