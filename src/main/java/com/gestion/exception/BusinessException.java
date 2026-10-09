package com.gestion.exception;

import com.gestion.enums.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode code;

    protected BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    protected BusinessException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
