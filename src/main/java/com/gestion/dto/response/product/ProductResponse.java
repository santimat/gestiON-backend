package com.gestion.dto.response.product;

import com.gestion.dto.response.category.CategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal suggestedPrice,
        Integer currentStock,
        Integer minStock,
        String imageUrl,
        CategoryResponse category,
        boolean active,
        LocalDateTime updatedAt
) {
}
