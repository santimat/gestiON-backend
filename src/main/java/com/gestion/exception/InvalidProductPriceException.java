package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InvalidProductPriceException extends BusinessException {

    public InvalidProductPriceException(ErrorCode code, String message) {
        super(code, message);
    }
}
