package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class OfferHasPaymentMethodsException extends BusinessException {

    public OfferHasPaymentMethodsException(ErrorCode code, String message) {
        super(code, message);
    }
}