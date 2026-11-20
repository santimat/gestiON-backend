package com.gestion.service.offer;

import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
@AllArgsConstructor
public class OfferDateValidator implements OfferValidationPipeline.OfferValidator {
    private final OfferDateValidatorService delegate;

    @Override
    public void validate(OfferValidationContext ctx) {
        if (ctx.offerId == null) {
            delegate.validateOfferDates(ctx.startDate, ctx.endDate, ctx.now);
        } else {
            delegate.validateOfferDatesForUpdate(ctx.startDate, ctx.endDate, ctx.now, ctx.existingOffer.getStartDate());
        }
    }
}