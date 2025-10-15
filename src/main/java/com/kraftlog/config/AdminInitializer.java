package com.kraftlog.config;

import com.kraftlog.entity.User;
import com.kraftlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.email:admin@kraftlog.com}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        // Check if admin user already exists
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("Admin user already exists with email: {}", adminEmail);
            return;
        }

        // Create admin user
        User admin = User.builder()
                .name("Admin")
                .surname("User")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .isAdmin(true)
                .build();

        userRepository.save(admin);
        log.info("Admin user created successfully with email: {}", adminEmail);
        log.info("Please change the admin password after first login!");
    }
}