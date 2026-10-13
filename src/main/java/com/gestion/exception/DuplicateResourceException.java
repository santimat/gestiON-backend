package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode code, String message) {
        super(code, message);
    }
}
