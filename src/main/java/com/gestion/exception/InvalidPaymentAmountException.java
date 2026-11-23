package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class InvalidPaymentAmountException extends BusinessException {

    public InvalidPaymentAmountException() {
        super(ErrorCode.INVALID_PAYMENT_AMOUNT, "Payment amount must be greater than zero and not exceed pending amount");
    }
}