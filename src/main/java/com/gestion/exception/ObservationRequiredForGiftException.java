package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class ObservationRequiredForGiftException extends BusinessException {

    public ObservationRequiredForGiftException() {
        super(ErrorCode.OBSERVATION_REQUIRED_FOR_GIFT, ErrorCode.OBSERVATION_REQUIRED_FOR_GIFT.getDefaultMessage());
    }
}