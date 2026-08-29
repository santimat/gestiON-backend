package com.gestion.dto.request.product;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        Long categoryId,
        Long commerceId,
        Integer currentStock,
        Integer minStock
) {
}
