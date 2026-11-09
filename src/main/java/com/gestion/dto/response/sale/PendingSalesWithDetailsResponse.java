package com.gestion.dto.response.sale;

import java.math.BigDecimal;
import java.util.List;

public record PendingSalesWithDetailsResponse(
        Long pendingSalesCount,
        BigDecimal totalPending,
        List<SaleResponse> pendingSales
) {
}