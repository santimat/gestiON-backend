package com.gestion.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Se valida al arranque: sin JWT_SECRET (fuera del perfil dev) la app no levanta, en vez de firmar
 * tokens con un secret conocido. HS256 exige al menos 256 bits; Keys.hmacShaKeyFor tambien lo
 * rechaza, pero con un mensaje menos claro.
 */
@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank(message = "jwt.secret (JWT_SECRET) is required")
        @Size(min = 32, message = "jwt.secret (JWT_SECRET) must be at least 32 characters (256 bits)")
        String secret,

        @NotNull
        @Positive
        Long expiration
) {
}
