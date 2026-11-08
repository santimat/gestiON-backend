package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class OfferHasProductsException extends BusinessException {

    public OfferHasProductsException(ErrorCode code, String message) {
        super(code, message);
    }
}
