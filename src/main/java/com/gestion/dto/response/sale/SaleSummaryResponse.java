package com.gestion.dto.response.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleSummaryResponse(
        Long id,
        Integer itemCount,
        SaleStatus status,
        String clientName,
        PaymentMethod paymentMethod,
        LocalDateTime createdAt,
        BigDecimal total
) {
}
