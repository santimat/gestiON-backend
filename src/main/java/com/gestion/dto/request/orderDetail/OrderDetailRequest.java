package com.gestion.dto.request.orderDetail;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderDetailRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Minimum quantity must be 1")
        Integer quantity,

        @NotNull
        BigDecimal unitPrice
) {
}

