package com.gestion.service.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Offer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class OfferValidationContext {
    // Input
    public Long commerceId;
    public Long offerId; // null for create
    public BigDecimal value;
    public DiscountType discountType;
    public Instant startDate;
    public Instant endDate;
    public Set<PaymentMethod> paymentMethods;
    public Offer existingOffer; // for update
    public Instant now;

    // Computed
    public Set<PaymentMethod> normalizedPaymentMethods;
}