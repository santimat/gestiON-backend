package com.gestion.dto.request.product;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal costPrice,
        BigDecimal salePrice,
        MultipartFile image,
        Long categoryId,
        Long commerceId,
        Integer currentStock,
        Integer minStock
) {
}
