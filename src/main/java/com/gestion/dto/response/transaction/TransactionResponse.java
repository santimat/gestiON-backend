    package com.gestion.dto.response.transaction;

import com.gestion.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        Long saleId,
        Instant createdAt
) {
}