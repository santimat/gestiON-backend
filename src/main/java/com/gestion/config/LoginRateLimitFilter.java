package com.gestion.config;

import com.gestion.enums.ErrorCode;
import com.gestion.properties.LoginRateLimitProperties;
import com.gestion.service.auth.AuthLoginAttemptLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Limita los intentos de login por IP antes de que el request llegue al controller.
@Slf4j
@Component
@AllArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    public static final String LOGIN_PATH = "/api/auth/login";

    private final AuthLoginAttemptLimiterService loginAttemptLimiterService;
    private final SecurityErrorWriter securityErrorWriter;
    private final LoginRateLimitProperties properties;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // con server.forward-headers-strategy configurado, getRemoteAddr ya es la IP real del cliente
        String clientIp = request.getRemoteAddr();

        if (loginAttemptLimiterService.registerAttemptAndCheckIpBlocked(clientIp)) {
            log.warn("Login rate limit exceeded for ip {}", clientIp);
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(properties.window().toSeconds()));
            securityErrorWriter.write(response, ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
