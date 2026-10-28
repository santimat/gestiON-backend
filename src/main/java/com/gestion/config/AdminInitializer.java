package com.gestion.config;

import com.gestion.enums.Role;
import com.gestion.model.User;
import com.gestion.properties.AdminProperties;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final AdminProperties adminProperties;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (userRepository.existsByEmail(adminProperties.email())) return;

        User admin = new User();
        admin.setEmail(adminProperties.email());
        admin.setPassword(passwordEncoder.encode(adminProperties.password()));
        admin.setRole(Role.SUDO);
        userRepository.save(admin);
    }
}
