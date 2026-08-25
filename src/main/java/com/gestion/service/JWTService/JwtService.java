package com.gestion.service.JWTService;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private final String secretWord;

    // Expiration time in milliseconds
    @Value("${jwt.expiration}")
    private final Long expirationTime;

}
