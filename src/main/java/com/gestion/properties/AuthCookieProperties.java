package com.gestion.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Atributos de la cookie de sesion. La duracion no se configura aca: se deriva de jwt.expiration
 * para que cookie y token venzan juntos.
 */
@Validated
@ConfigurationProperties(prefix = "app.auth.cookie")
public record AuthCookieProperties(
        @NotBlank
        String name,

        boolean secure,

        @NotBlank
        @Pattern(regexp = "Strict|Lax|None", message = "app.auth.cookie.same-site must be Strict, Lax or None")
        String sameSite
) {
}
