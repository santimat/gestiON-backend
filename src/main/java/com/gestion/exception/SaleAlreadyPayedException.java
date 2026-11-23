package com.gestion.exception;

import com.gestion.enums.ErrorCode;

public class SaleAlreadyPayedException extends BusinessException {

    public SaleAlreadyPayedException() {
        super(ErrorCode.SALE_ALREADY_PAYED, "Sale is already fully paid");
    }
}