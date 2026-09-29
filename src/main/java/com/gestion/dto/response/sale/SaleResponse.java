package com.gestion.dto.response.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.math.BigDecimal;
import java.util.Date;


public record SaleResponse(
        Long id,
        String userName,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        Double discount,
        BigDecimal total,
        Date createdAt,
        String observations,
        SaleStatus status

) {
}
