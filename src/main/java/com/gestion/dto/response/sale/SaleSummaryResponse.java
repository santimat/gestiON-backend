package com.gestion.dto.response.sale;

import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;

public record SaleSummaryResponse(
        Long id,
        Integer itemCount,
        SaleStatus status,
        String clientName,
        BigDecimal total
) {
}
