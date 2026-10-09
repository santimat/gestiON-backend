package com.gestion.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_CREDENTIALS(401, "Invalid email or password"),
    ACCOUNT_DISABLED(403, "The account is disabled"),
    UNAUTHORIZED(401, "Authentication is required"),
    ACCESS_DENIED(403, "Access denied"),

    USER_NOT_FOUND(404, "User not found"),
    CLIENT_NOT_FOUND(404, "Client not found"),
    CATEGORY_NOT_FOUND(404, "Category not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    COMMERCE_NOT_FOUND(404, "Commerce not found"),
    SALE_NOT_FOUND(404, "Sale not found"),
    ENDPOINT_NOT_FOUND(404, "Endpoint not found"),

    EMAIL_ALREADY_EXISTS(409, "Email is already registered"),
    CLIENT_DNI_ALREADY_EXISTS(409, "Client DNI is already registered"),
    COMMERCE_ADDRESS_ALREADY_EXISTS(409, "Commerce address is already registered"),
    COMMERCE_CUIT_ALREADY_EXISTS(409, "Commerce CUIT is already registered"),

    INVALID_FILE_TYPE(400, "File type is not allowed"),
    FILE_TOO_LARGE(413, "File size exceeds the maximum limit"),
    FILE_UPLOAD_FAILED(500, "File could not be uploaded"),
    FILE_DELETE_FAILED(500, "File could not be deleted"),
    FILE_PROCESSING_FAILED(500, "File could not be processed"),

    VALIDATION_ERROR(400, "Request validation failed"),

    INVALID_REQUEST_BODY(400, "Request body is malformed"),
    INVALID_PARAMETER(400, "A request parameter is invalid"),
    MISSING_PARAMETER(400, "A required request parameter is missing"),
    METHOD_NOT_ALLOWED(405, "HTTP method is not supported for this endpoint"),
    UNSUPPORTED_MEDIA_TYPE(415, "Content type is not supported"),

    DATA_INTEGRITY_VIOLATION(409, "The operation violates data integrity constraints"),
    INTERNAL_ERROR(500, "An unexpected internal error occurred");

    private final int status;
    private final String defaultMessage;

    ErrorCode(int status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
