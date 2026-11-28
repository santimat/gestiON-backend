package com.gestion.dto.response.auth;

public record CsrfTokenResponse(
        String headerName,
        String token
) {
}
