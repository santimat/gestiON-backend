package com.gestion.dto.response.orderDetail;

import java.math.BigDecimal;

public record OrderDetailResponse(
        Long id,
        Long productId,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
