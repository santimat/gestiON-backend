package com.gestion.dto.request.transaction;

import com.gestion.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        Long saleId
) {
}