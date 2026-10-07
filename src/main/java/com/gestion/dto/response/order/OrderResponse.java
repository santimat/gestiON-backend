package com.gestion.dto.response.order;

import com.gestion.dto.response.orderDetail.OrderDetailResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long commerceId,
        String businessName,
        String description,
        BigDecimal total,
        List<OrderDetailResponse> details,
        LocalDateTime createdAt

) {
}
