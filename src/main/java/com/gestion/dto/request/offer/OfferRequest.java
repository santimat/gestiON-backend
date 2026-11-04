package com.gestion.dto.request.offer;

import com.gestion.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record OfferRequest(
        @NotNull(message = "Offer value is required")
        @Positive(message = "Offer value must be a positive number")
        BigDecimal value,
        @NotNull(message = "Start date is required")
        String startDate,
        @NotNull(message = "End date is required")
        String endDate,
        
        Set<PaymentMethod> paymentMethods
) {
}
