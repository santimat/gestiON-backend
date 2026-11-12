package com.gestion.dto.response.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        Long clientId,
        String clientName,
        String sellPointName,
        String userName,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        DiscountType paymentMethodDiscountType,
        BigDecimal paymentMethodDiscount,
        BigDecimal total,
        Instant createdAt,
        String observations,
        SaleStatus status,
        List<SaleDetailResponse> details
) {
}
