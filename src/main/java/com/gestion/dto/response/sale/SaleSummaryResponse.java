package com.gestion.dto.response.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SaleSummaryResponse(
        Long id,
        Integer itemCount,
        SaleStatus status,
        String clientName,
        PaymentMethod paymentMethod,
        Instant createdAt,
        BigDecimal subtotal,
        BigDecimal discount,
        DiscountType discountType,
        BigDecimal total
) {
}
