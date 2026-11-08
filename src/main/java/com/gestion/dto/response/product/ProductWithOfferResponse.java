package com.gestion.dto.response.product;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.enums.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductWithOfferResponse(
        Long id,
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal suggestedPrice,
        Double profitMultiplier,
        Integer currentStock,
        Integer minStock,
        String imageUrl,
        Long offerId,
        BigDecimal offerValue,
        DiscountType offerDiscountType,
        BigDecimal discountedPrice,
        CategoryResponse category,
        boolean active,
        Instant updatedAt
) {
}
