package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException(String message) {
        super(ErrorCode.INVALID_CREDENTIALS, message);
    }
}
