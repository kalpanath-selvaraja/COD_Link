package com.ijse.codlink.security;

import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.UserRole;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.username}")
    private String defaultAdminUsername;


    @Value("${app.default-admin.password}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) {
        Optional<User> optionalAdmin = userRepository.findByUsername(defaultAdminUsername);

        if (optionalAdmin.isEmpty()) {
            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole(UserRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            log.warn("Seeded default admin account — username: '{}'. " +
                            "This uses a hardcoded default password from application.properties. " +
                            "Change it after first login if this is going anywhere beyond coursework evaluation.",
                    defaultAdminUsername);
        } else {
            log.info("Admin seeding skipped — username '{}' already exists (role: {})",
                    defaultAdminUsername, optionalAdmin.get().getRole());


        }
    }
}