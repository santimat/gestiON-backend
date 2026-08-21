package com.gestion.service.auth;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthRegisterService {
    private final PasswordEncoder passwordEncoder;
}
