package com.gestion.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Limites del login dentro de una ventana fija:
 * - maxAttemptsPerIp: intentos (exitosos o no) por IP; frena barridos de muchas cuentas desde un origen.
 * - maxFailuresPerEmail: fallos por email; frena la fuerza bruta sobre una cuenta desde varias IPs.
 */
@Validated
@ConfigurationProperties(prefix = "app.auth.login-rate-limit")
public record LoginRateLimitProperties(
        @Positive
        int maxAttemptsPerIp,

        @Positive
        int maxFailuresPerEmail,

        @NotNull
        Duration window
) {
}
