package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InactiveResourceException extends BusinessException {

    public InactiveResourceException(ErrorCode code, String message) {
        super(code, message);
    }
}
