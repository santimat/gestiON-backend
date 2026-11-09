package com.gestion.dto.response.sale;

import java.math.BigDecimal;

public record ProfitByPeriodResponse(
        BigDecimal profit
) {
}