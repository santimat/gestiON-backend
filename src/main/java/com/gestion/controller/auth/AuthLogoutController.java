package com.gestion.controller.auth;

import com.gestion.service.auth.AuthCookieBuilderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/logout")
@AllArgsConstructor
public class AuthLogoutController {
    private final AuthCookieBuilderService authCookieBuilderService;

    @PostMapping
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieBuilderService.buildExpiredCookie().toString())
                .body("Logout successfully");
    }
}
