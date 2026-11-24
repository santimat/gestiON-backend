package com.gestion.exception;

import com.gestion.enums.ErrorCode;


public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(ErrorCode code, String message) {
        super(code, message);
    }
}
