package com.gestion.exception;

import com.gestion.enums.ErrorCode;

/**
 * Un parametro de la request tiene un valor invalido.
 *
 * Se usa para los parametros que Spring no valida por si solo (page, size, sortBy, sortDir) y
 * que antes terminaban en un 500 INTERNAL_ERROR cuando el cliente mandaba un valor invalido.
 */
public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(ErrorCode code, String message) {
        super(code, message);
    }
}
