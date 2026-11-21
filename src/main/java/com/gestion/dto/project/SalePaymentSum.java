package com.gestion.dto.project;

import java.math.BigDecimal;

public record SalePaymentSum(
        Long saleId,
        BigDecimal totalPaid
) {
}
