package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.PartialPaymentExceedsTotalException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SalePartialPaymentValidatorService {

    public void validate(SaleRequest request, BigDecimal total) {
        if (request.paymentMethod() == PaymentMethod.CREDIT_ACCOUNT
                && request.partiallyPaidAmount() != null
                && request.partiallyPaidAmount().compareTo(total) > 0) {
            throw new PartialPaymentExceedsTotalException();
        }
    }
}