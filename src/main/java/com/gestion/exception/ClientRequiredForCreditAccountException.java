package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class ClientRequiredForCreditAccountException extends BusinessException {

    public ClientRequiredForCreditAccountException() {
        super(ErrorCode.CLIENT_REQUIRED_FOR_CREDIT_ACCOUNT, "Client is required for credit account payment method");
    }
}