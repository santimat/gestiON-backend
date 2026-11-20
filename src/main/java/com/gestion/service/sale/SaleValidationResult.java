package com.gestion.service.sale;

import com.gestion.model.SaleDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SaleValidationResult(
        Map<Long, Integer> quantitiesByProductId,
        List<SaleDetail> details,
        BigDecimal subtotal
) {
}