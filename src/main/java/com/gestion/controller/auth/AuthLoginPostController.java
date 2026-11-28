package com.gestion.controller.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.mappers.AuthenticatedUserMapper;
import com.gestion.service.auth.AuthCookieBuilderService;
import com.gestion.service.auth.AuthLoginService;
import com.gestion.service.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/login")
@AllArgsConstructor
public class AuthLoginPostController {
    private final AuthLoginService authLoginService;
    private final JwtService jwtService;
    private final AuthCookieBuilderService authCookieBuilderService;

    @PostMapping
    public ResponseEntity<AuthenticatedUserResponse> login(@RequestBody @Valid UserLoginRequest userRequest) {
        String jwtToken = authLoginService.login(userRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieBuilderService.buildSessionCookie(jwtToken).toString())
                .body(AuthenticatedUserMapper.toTokenResponseFromClaims(jwtService.getClaimsFromToken(jwtToken)));
    }
}
