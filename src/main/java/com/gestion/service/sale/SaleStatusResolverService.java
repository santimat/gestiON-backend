package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import org.springframework.stereotype.Service;


@Service
public class SaleStatusResolverService {

    public SaleStatus resolveStatus(SaleRequest request) {
        return request.paymentMethod() == PaymentMethod.CREDIT_ACCOUNT
                ? SaleStatus.PENDING
                : SaleStatus.PAY;
    }
}
