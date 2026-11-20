package com.gestion.service.offer;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(4)
@AllArgsConstructor
public class OfferPaymentMethodValidator implements OfferValidationPipeline.OfferValidator {
    private final OfferPaymentMethodsValidatorService delegate;

    @Override
    public void validate(OfferValidationContext ctx) {
        if (ctx.offerId != null) {
            delegate.validateNoAssignedProducts(ctx.offerId, ctx.commerceId, ctx.normalizedPaymentMethods);
        }
    }
}