package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.ClientRequiredForCreditAccountException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SalePaymentMethodValidatorService {

    public void validate(SaleRequest request) {
        if (request.paymentMethod() == PaymentMethod.CREDIT_ACCOUNT && request.clientId() == null) {
            throw new ClientRequiredForCreditAccountException();
        }
    }
}