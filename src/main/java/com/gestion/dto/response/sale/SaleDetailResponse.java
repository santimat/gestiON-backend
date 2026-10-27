package com.gestion.dto.response.sale;

import java.math.BigDecimal;

public record SaleDetailResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity
) {
}
