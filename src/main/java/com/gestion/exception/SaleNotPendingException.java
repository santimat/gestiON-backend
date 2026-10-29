package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class SaleNotPendingException extends BusinessException {

    public SaleNotPendingException(String message) {
        super(ErrorCode.SALE_NOT_PENDING, message);
    }
}
