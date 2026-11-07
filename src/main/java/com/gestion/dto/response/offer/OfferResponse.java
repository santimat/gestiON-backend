package com.gestion.dto.response.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record OfferResponse(
        Long id,
        BigDecimal value,
        DiscountType discountType,
        Instant startDate,
        Instant endDate,
        OfferStatus status,
        Set<PaymentMethod> paymentMethods
) {
}
