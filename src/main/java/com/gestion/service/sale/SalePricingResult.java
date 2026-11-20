package com.gestion.service.sale;

import com.gestion.enums.SaleStatus;
import java.math.BigDecimal;

public record SalePricingResult(
        BigDecimal total,
        SaleStatus status
) {
}