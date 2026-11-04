package com.gestion.dto.response.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OfferResponse(
        Long id,
        BigDecimal value,
        DiscountType discountType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Set<PaymentMethod> paymentMethods
) {
}
