package com.gestion.controller.auth;

import com.gestion.dto.response.auth.CsrfTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/csrf")
public class AuthCsrfGetController {

    @GetMapping
    public ResponseEntity<CsrfTokenResponse> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken()));
    }
}
