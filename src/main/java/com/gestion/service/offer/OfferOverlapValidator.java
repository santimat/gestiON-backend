package com.gestion.service.offer;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(3)
@AllArgsConstructor
public class OfferOverlapValidator implements OfferValidationPipeline.OfferValidator {
    private final OfferOverlapValidatorService delegate;

    @Override
    public void validate(OfferValidationContext ctx) {
        delegate.validateNoOverlap(
                ctx.commerceId,
                ctx.offerId,
                ctx.startDate,
                ctx.endDate,
                ctx.value,
                ctx.discountType,
                ctx.paymentMethods);
    }
}