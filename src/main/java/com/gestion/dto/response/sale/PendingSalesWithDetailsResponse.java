package com.gestion.dto.response.sale;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public record PendingSalesWithDetailsResponse(
        Long pendingSalesCount,
        BigDecimal totalPending,
        Page<SaleResponse> pendingSales
) {
}
