package com.gestion.controller.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.dto.response.user.UserTokenResponse;
import com.gestion.mappers.UserMapper;
import com.gestion.service.auth.AuthLoginService;
import com.gestion.service.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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

    @PostMapping
    public ResponseEntity<UserTokenResponse> login(@RequestBody @Valid UserLoginRequest userRequest,
                                                   HttpServletResponse response) {
        String jwtToken = authLoginService.login(userRequest);

        Cookie cookie = new Cookie("token", jwtToken);
        cookie.setHttpOnly(true);
        // https is not required
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        return ResponseEntity.ok(UserMapper.toResponseFromClaims(jwtService.getClaimsFromToken(jwtToken)));
    }
}
