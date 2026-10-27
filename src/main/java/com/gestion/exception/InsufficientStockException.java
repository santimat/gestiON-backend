package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(ErrorCode code, String message) {
        super(code, message);
    }
}
