package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InvalidDiscountException extends BusinessException {

    public InvalidDiscountException(ErrorCode code, String message) {
        super(code, message);
    }
}
