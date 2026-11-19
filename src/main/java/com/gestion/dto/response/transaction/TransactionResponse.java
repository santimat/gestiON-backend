package com.gestion.dto.response.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        BigDecimal revenue,
        BigDecimal outflow,
        Long saleId,
        Instant createdAt
) {
}