package com.gestion.dto.request.commerce;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CommerceDefaultMinStockRequest(
        @NotNull(message = "Default minimum stock is required")
        @Positive(message = "Default minimum stock must be a positive number")
        Integer defaultMinStock
) {
}
