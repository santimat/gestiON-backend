package com.gestion.dto.request.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

public record SaleRequest(
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        Double discount,
        BigDecimal total,
        LocalDateTime createdAt,
        String observations,
        SaleStatus status

) {
}
