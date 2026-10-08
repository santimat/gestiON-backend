package com.gestion.controller.auth;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.mappers.AuthenticatedUserMapper;
import com.gestion.service.auth.AuthCheckerStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
public class AuthCheckController {

    private final AuthCheckerStatusService authCheckerStatusService;

    @GetMapping
    public ResponseEntity<AuthenticatedUserResponse> checkAuth(@AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (authenticatedUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // TODO: preguntar si conviene tener una tabla intermedia para la relación usuarios comercios.
        if (authenticatedUser.getCommerceId() != null && !authCheckerStatusService.isCommerceUserActive(authenticatedUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(AuthenticatedUserMapper.toTokenResponseFromUserPrincipal(authenticatedUser));
    }
}
