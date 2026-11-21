package com.gestion.dto.project.sale;

import java.math.BigDecimal;

public record SalePaymentSum(
        Long saleId,
        BigDecimal totalPaid
) {
}
