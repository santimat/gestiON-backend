package com.gestion.dto.response.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        Long clientId,
        Long sellPointId,
        String userName,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        DiscountType discountType,
        BigDecimal discount,
        BigDecimal total,
        LocalDateTime createdAt,
        String observations,
        SaleStatus status,
        List<SaleDetailResponse> details
) {
}
