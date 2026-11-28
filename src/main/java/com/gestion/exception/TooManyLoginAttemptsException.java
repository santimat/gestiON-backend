package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class TooManyLoginAttemptsException extends BusinessException {

    public TooManyLoginAttemptsException() {
        super(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS, ErrorCode.TOO_MANY_LOGIN_ATTEMPTS.getDefaultMessage());
    }
}
