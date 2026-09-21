package com.gestion.controller.auth;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.mappers.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/me")
@AllArgsConstructor
public class AuthCheckController {

    @GetMapping
    public ResponseEntity<AuthenticatedUserResponse> checkAuth(@AuthenticationPrincipal UserPrincipal authenticatedUser) {
        if (authenticatedUser == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        return ResponseEntity.ok(UserMapper.toTokenResponseFromUserPrincipal(authenticatedUser));
    }
}
