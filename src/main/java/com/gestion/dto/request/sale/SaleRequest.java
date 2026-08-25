package com.gestion.dto.request.sale;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;

import java.util.Date;

public record SaleRequest(
        PaymentMethod paymentMethod,
        Float subtotal,
        Double discount,
        Float total,
        Date createdAt,
        String observations,
        SaleStatus status

) {
}
