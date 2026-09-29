package com.gestion.dto.request.sale;

import com.gestion.enums.PaymentMethod;

import java.math.BigDecimal;

public record SaleRequest(
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        Double discount,
        String observations
) {
}
