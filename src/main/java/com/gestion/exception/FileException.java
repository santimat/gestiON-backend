package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class FileException extends BusinessException {

    public FileException(ErrorCode code, String message) {
        super(code, message);
    }

    public FileException(ErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
