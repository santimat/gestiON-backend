package com.gestion.dto.response.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;

import java.math.BigDecimal;

public record OfferPaymentMethodResponse(
        PaymentMethod paymentMethod,
        BigDecimal value,
        DiscountType discountType
) {
}