package com.enterprise.ecommerce.user.config;

import com.enterprise.ecommerce.user.entity.User;
import com.enterprise.ecommerce.user.enums.Role;
import com.enterprise.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@ecommerce.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("9999999999")
                .address("Admin Office")
                .role(Role.ADMIN)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .firstName("John")
                .lastName("Customer")
                .email("customer@ecommerce.com")
                .password(passwordEncoder.encode("customer123"))
                .phone("8888888888")
                .address("123 Main St")
                .role(Role.CUSTOMER)
                .active(true)
                .build());
    }
}
