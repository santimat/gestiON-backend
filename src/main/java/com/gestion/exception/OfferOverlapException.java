package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class OfferOverlapException extends BusinessException {
    public OfferOverlapException(ErrorCode code, String message) {
        super(code, message);
    }
}