package com.plenkov.cloudstorage;

import com.plenkov.cloudstorage.config.SecurityConfig;
import com.plenkov.cloudstorage.dto.auth.UserDto;
import com.plenkov.cloudstorage.dto.auth.UserRegisterRequestDto;
import com.plenkov.cloudstorage.dto.auth.UserSignInRequestDto;
import com.plenkov.cloudstorage.exception.AuthException;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import com.plenkov.cloudstorage.repository.UserRepository;
import com.plenkov.cloudstorage.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        AuthService.class,
        UserRepository.class,
        SecurityConfig.class
})
@EnableAutoConfiguration
@Testcontainers
public class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("cloudstorage")
            .withUsername("testuser")
            .withPassword("testpass");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @Transactional
    void testRegisterUser_Success() {
        UserRegisterRequestDto user = new UserRegisterRequestDto("test_user", "test_password");

        authService.register(user);

        var userOptional = userRepository.findByLogin(user.getUsername());

        assertTrue(userOptional.isPresent(), "Пользователь должен быть в базе");
        assertEquals(user.getUsername(), userOptional.get().getLogin());
        assertNotEquals(encoder.encode(user.getPassword()), userOptional.get().getPassword());
    }

    @Test
    @Transactional
    void testRegisterUser_AlreadyExists_ThrowsException() {
        UserRegisterRequestDto user = new UserRegisterRequestDto("test_user", "test_password");

        authService.register(user);

        assertThrows(UserAlreadyExistException.class, () -> {
            authService.register(user);
        }, "Должно быть выброшено исключение, так как пользователь уже существует");
    }

    @Test
    @Transactional
    void test_Authenticate_Success() {
        UserRegisterRequestDto userRegister = new UserRegisterRequestDto("test_user", "test_password");

        authService.register(userRegister);

        UserSignInRequestDto userSignInRequestDto = new UserSignInRequestDto("test_user", "test_password");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDto authenticatedUser = authService.authenticate(userSignInRequestDto, request, response);

        assertEquals(authenticatedUser.username(), userSignInRequestDto.username());
    }

    @Test
    @Transactional
    void test_Authenticate_Failed() {
        UserRegisterRequestDto userRegister = new UserRegisterRequestDto("test_user", "test_password");

        authService.register(userRegister);

        UserSignInRequestDto userSignInRequestDto = new UserSignInRequestDto("test_user", "wrong_password");

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        userSignInRequestDto.username(),
                        userSignInRequestDto.password()
                );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authenticationManager.authenticate(token)).thenThrow(new BadCredentialsException("Failed"));

        assertThrows(AuthException.class, () -> {
            authService.authenticate(userSignInRequestDto, request, response);
        }, "Должно быть выброшено исключение AuthException при неверных учетных данных");
    }
}
