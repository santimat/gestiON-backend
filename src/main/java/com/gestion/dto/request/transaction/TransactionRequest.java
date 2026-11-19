package com.gestion.dto.request.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Revenue is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Revenue must be zero or positive")
        BigDecimal revenue,

        @NotNull(message = "Outflow is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Outflow must be zero or positive")
        BigDecimal outflow,

        @NotNull(message = "Sale ID is required")
        Long saleId
) {
}