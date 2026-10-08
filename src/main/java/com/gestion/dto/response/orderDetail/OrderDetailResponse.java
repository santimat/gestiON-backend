package com.gestion.dto.response.orderDetail;

import com.gestion.model.Product;

import java.math.BigDecimal;

public record OrderDetailResponse(
        Long id,
        Product product,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
