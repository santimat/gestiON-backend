package com.gestion.dto.request.commerce;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommerceProfitMultiplierRequest(
        @NotNull(message = "Profit multiplier is required")
        @Positive(message = "Profit multiplier must be a positive number")
        Double profitMultiplier
) {
}
