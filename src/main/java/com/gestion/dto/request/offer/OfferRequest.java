package com.gestion.dto.request.offer;

import jakarta.validation.constraints.Positive;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public record OfferRequest(
        @NotNull("Product ID is required")
        @Positive(message = "Product ID must be a positive number")
        Long productId,
        @NotNull("Offer value is required")
        @Positive(message = "Offer value must be a positive number")
        BigDecimal value,
        @NotNull("Start date is required")
        String startDate,
        @NotNull("End date is required")
        String endDate
) {
}
