package com.gestion.dto.response.exception;

public record ExceptionResponse(
        int statusCode,
        String message
) {
}
