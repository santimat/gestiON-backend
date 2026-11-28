package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class StaleResourceException extends BusinessException {

    public StaleResourceException(String message) {
        super(ErrorCode.CONCURRENT_MODIFICATION, message);
    }
}
