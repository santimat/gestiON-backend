package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class CashierSellPointRequiredException extends BusinessException {

    public CashierSellPointRequiredException(ErrorCode code, String message) {
        super(code, message);
    }
}