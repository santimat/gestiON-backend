package com.gestion.dto.project.client;

import java.math.BigDecimal;

public record ClientPendingPaymentSum(
        Long clientId,
        BigDecimal totalPaid
) {
}
