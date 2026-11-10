package com.gestion.dto.response.sale;

public record TopProductsSoldResponse(
        Long productId,
        String productName,
        Long quantity
) {
}