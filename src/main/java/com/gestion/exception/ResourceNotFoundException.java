package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(ErrorCode code, String message) {
        super(code, message);
    }
}
