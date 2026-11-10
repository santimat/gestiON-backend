package com.gestion.dto.response.product;

public record ProductLowStockResponse(
        Long productId,
        String productName,
        Integer minStock,
        Integer currentStock
) {
}