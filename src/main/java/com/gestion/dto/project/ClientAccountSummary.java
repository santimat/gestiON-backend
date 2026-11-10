package com.gestion.dto.project;

import java.math.BigDecimal;

public record ClientAccountSummary(
        Long clientId,
        Long salesCount,
        Long pendingSalesCount,
        BigDecimal totalSpent,
        BigDecimal totalPending
) {
}
