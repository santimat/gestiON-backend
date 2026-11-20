package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class PartialPaymentExceedsTotalException extends BusinessException {

    public PartialPaymentExceedsTotalException() {
        super(ErrorCode.PARTIAL_PAYMENT_EXCEEDS_TOTAL, "Partially paid amount cannot exceed sale total");
    }
}