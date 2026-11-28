package com.gestion.controller.auth;

import com.gestion.config.UserPrincipal;
import com.gestion.service.auth.AuthCookieBuilderService;
import com.gestion.service.user.UserSessionsRevokerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/logout-all")
@AllArgsConstructor
public class AuthLogoutAllPostController {
    private final UserSessionsRevokerService userSessionsRevokerService;
    private final AuthCookieBuilderService authCookieBuilderService;

    @PostMapping
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal UserPrincipal authenticatedUser) {
        userSessionsRevokerService.revokeAllSessions(authenticatedUser.getId());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieBuilderService.buildExpiredCookie().toString())
                .build();
    }
}
