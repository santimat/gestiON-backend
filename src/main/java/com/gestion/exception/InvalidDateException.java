package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InvalidDateException extends BusinessException {

    public InvalidDateException(ErrorCode code, String message) {
        super(code, message);
    }
}
