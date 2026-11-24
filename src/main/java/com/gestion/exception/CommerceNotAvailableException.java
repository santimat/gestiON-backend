package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class CommerceNotAvailableException extends BusinessException {

    public CommerceNotAvailableException(ErrorCode code, String message) {
        super(code, message);
    }
}
