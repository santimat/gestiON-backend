package com.gestion.dto.response.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExceptionResponse(
        int statusCode,
        String code,
        String message,
        List<FieldError> fields
) {

    public record FieldError(String field, String message) {
    }

    public ExceptionResponse(int statusCode, String code, String message) {
        this(statusCode, code, message, null);
    }
}
