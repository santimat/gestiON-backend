package com.gestion.dto.request.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record OfferUpdateRequest(
        @NotNull(message = "Offer value is required")
        @Positive(message = "Offer value must be a positive number")
        BigDecimal value,
        @NotNull(message = "Discount type is required")
        DiscountType discountType,
        @NotNull(message = "Start date is required")
        Instant startDate,
        @NotNull(message = "End date is required")
        Instant endDate,
        Set<PaymentMethod> paymentMethods
) {
}