package com.gestion.dto.response.product;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        String imageUrl,
        ProductStatus status,
        CategoryResponse category
) {
}
