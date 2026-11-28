package com.gestion.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Credenciales del SUDO inicial. Son opcionales (sin defaults fuera del perfil dev): si faltan,
 * AdminInitializer no crea el usuario. Si se definen, tienen que cumplir la politica de contraseñas.
 */
@Validated
@ConfigurationProperties(prefix = "admin")
public record AdminProperties(
        String name,

        @Email(message = "admin.email (ADMIN_EMAIL) must be a valid email")
        String email,

        // vacio = no configurado (ADMIN_PASSWORD sin definir); si se define, 8 a 72 caracteres
        @Pattern(regexp = "^$|^.{8,72}$", message = "admin.password (ADMIN_PASSWORD) must be between 8 and 72 characters")
        String password
) {

    public boolean isConfigured() {
        return StringUtils.hasText(email) && StringUtils.hasText(password);
    }
}
