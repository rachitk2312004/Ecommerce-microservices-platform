package com.enterprise.ecommerce.user.service;

import com.enterprise.ecommerce.user.dto.LoginRequest;
import com.enterprise.ecommerce.user.dto.RegisterRequest;
import com.enterprise.ecommerce.user.entity.User;
import com.enterprise.ecommerce.user.enums.Role;
import com.enterprise.ecommerce.user.exception.DuplicateResourceException;
import com.enterprise.ecommerce.user.exception.UnauthorizedException;
import com.enterprise.ecommerce.user.mapper.UserMapper;
import com.enterprise.ecommerce.user.repository.UserRepository;
import com.enterprise.ecommerce.user.security.JwtUtil;
import com.enterprise.ecommerce.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserServiceImpl userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password123");
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("token");

        var result = userService.register(registerRequest);
        assertNotNull(result.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> userService.register(registerRequest));
    }

    @Test
    void login_invalidCredentials() {
        LoginRequest login = new LoginRequest();
        login.setEmail("test@test.com");
        login.setPassword("wrong");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> userService.login(login));
    }
}
