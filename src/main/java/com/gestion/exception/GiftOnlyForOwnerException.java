package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class GiftOnlyForOwnerException extends BusinessException {

    public GiftOnlyForOwnerException() {
        super(ErrorCode.GIFT_ONLY_FOR_OWNER, ErrorCode.GIFT_ONLY_FOR_OWNER.getDefaultMessage());
    }
}