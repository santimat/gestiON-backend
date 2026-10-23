package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class ClientNotFoundException extends BusinessException {
    public ClientNotFoundException(Long clientId) {
        super(ErrorCode.CLIENT_NOT_FOUND, "Client with id " + clientId + " not found");
    }
}