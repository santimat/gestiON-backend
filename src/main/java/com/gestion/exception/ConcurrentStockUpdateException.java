package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class ConcurrentStockUpdateException extends BusinessException {

    public ConcurrentStockUpdateException(ErrorCode code, String message) {
        super(code, message);
    }
}