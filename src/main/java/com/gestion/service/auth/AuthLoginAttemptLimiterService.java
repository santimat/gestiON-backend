package com.gestion.service.auth;

import com.gestion.properties.LoginRateLimitProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limit del login con cache en memoria (Caffeine)
 * - Por IP: cuenta todos los intentos (lo usa LoginRateLimitFilter antes de procesar el request).
 * - Por email: cuenta solo los fallos y se resetea con un login exitoso (lo usa AuthLoginService).
 */
@Service
public class AuthLoginAttemptLimiterService {

    private static final long MAX_TRACKED_KEYS = 100_000;

    private final LoginRateLimitProperties properties;
    private final Cache<String, AtomicInteger> attemptsByIp;
    private final Cache<String, AtomicInteger> failuresByEmail;

    public AuthLoginAttemptLimiterService(LoginRateLimitProperties properties) {
        this.properties = properties;
        this.attemptsByIp = newCounterCache();
        this.failuresByEmail = newCounterCache();
    }

    /**
     * Registra un intento desde la IP y devuelve true si supera el limite de la ventana.
     */
    public boolean registerAttemptAndCheckIpBlocked(String ip) {
        int attempts = attemptsByIp.get(ip, key -> new AtomicInteger()).incrementAndGet();
        return attempts > properties.maxAttemptsPerIp();
    }

    public boolean isEmailBlocked(String email) {
        AtomicInteger failures = failuresByEmail.getIfPresent(normalize(email));
        return failures != null && failures.get() >= properties.maxFailuresPerEmail();
    }

    public void registerEmailFailure(String email) {
        failuresByEmail.get(normalize(email), key -> new AtomicInteger()).incrementAndGet();
    }

    public void resetEmailFailures(String email) {
        failuresByEmail.invalidate(normalize(email));
    }

    private Cache<String, AtomicInteger> newCounterCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(properties.window())
                .maximumSize(MAX_TRACKED_KEYS)
                .build();
    }

    // la comparacion de emails en la base es case-insensitive (collation _ci): el contador tambien
    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
