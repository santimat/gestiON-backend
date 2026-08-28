package com.gestion.dto.response.product;

import com.gestion.enums.ProductStatus;
import com.gestion.model.Category;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        String imageUrl,
        Category category,
        ProductStatus status
) {
}
// aca revisemos las variables, capaz faltan mostrar algunas