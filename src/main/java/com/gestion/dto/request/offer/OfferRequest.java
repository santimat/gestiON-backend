package com.gestion.dto.request.offer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OfferRequest(
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be a positive number")
        Long productId,
        @NotNull(message = "Offer value is required")
        @Positive(message = "Offer value must be a positive number")
        BigDecimal value,
        @NotNull(message = "Start date is required")
        String startDate,
        @NotNull(message = "End date is required")
        String endDate
) {
}
