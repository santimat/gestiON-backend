package com.gestion.dto.request.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.util.Date;

public record SaleRequest(
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        Double discount,
        BigDecimal total,
        Date createdAt,
        String observations,
        SaleStatus status

) {
}
