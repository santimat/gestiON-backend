package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class CashierWithoutSellPointException extends BusinessException {

    public CashierWithoutSellPointException() {
        super(ErrorCode.CASHIER_WITHOUT_SELL_POINT, ErrorCode.CASHIER_WITHOUT_SELL_POINT.getDefaultMessage());
    }
}
