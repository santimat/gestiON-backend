package com.gestion.service.auth;

import com.gestion.properties.AuthCookieProperties;
import com.gestion.service.jwt.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class AuthCookieBuilderService {

    private final AuthCookieProperties cookieProperties;
    private final JwtService jwtService;

    public ResponseCookie buildSessionCookie(String jwtToken) {
        return baseCookie(jwtToken)
                .maxAge(Duration.ofMillis(jwtService.getExpirationMillis()))
                .build();
    }

    // borrar una cookie = reenviarla con los mismos atributos (nombre, path, flags) y Max-Age=0
    public ResponseCookie buildExpiredCookie() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(cookieProperties.name(), value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path("/");
    }
}
