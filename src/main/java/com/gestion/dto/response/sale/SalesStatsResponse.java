package com.gestion.dto.response.sale;

import java.math.BigDecimal;
import java.util.List;

public record SalesStatsResponse(
        long salesCount,
        BigDecimal averageSaleValue,
        List<PeriodProductCount> productsSoldByPeriod,
        BigDecimal profit
) {
}