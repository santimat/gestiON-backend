package com.gestion.controller.auth;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.mappers.AuthenticatedUserMapper;
import com.gestion.service.auth.AuthCheckerStatusService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/me")
@AllArgsConstructor
public class AuthCheckController {

    private final AuthCheckerStatusService authCheckerStatusService;

    @GetMapping
    public ResponseEntity<AuthenticatedUserResponse> checkAuth(@AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (authenticatedUser == null)
            throw new AuthenticationCredentialsNotFoundException("No authenticated user");

        // TODO: preguntar si conviene tener una tabla intermedia para la relación usuarios comercios.
        if (authenticatedUser.getCommerceId() != null && !authCheckerStatusService.isCommerceUserActive(authenticatedUser.getEmail()))
            throw new InactiveResourceException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled");

        return ResponseEntity.ok(AuthenticatedUserMapper.toTokenResponseFromUserPrincipal(authenticatedUser));
    }
}
