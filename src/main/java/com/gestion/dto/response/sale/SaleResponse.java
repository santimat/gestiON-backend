package com.gestion.dto.response.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.util.Date;


public record SaleResponse(
        Long id,
        String userName,
        PaymentMethod paymentMethod,
        Float subtotal,
        Double discount,
        Float total,
        Date createdAt,
        String observations,
        SaleStatus status

) {
}
