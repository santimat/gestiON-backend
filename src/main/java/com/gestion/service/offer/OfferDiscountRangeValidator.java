package com.gestion.service.offer;

import com.gestion.service.discount.DiscountRangeValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
@AllArgsConstructor
public class OfferDiscountRangeValidator implements OfferValidationPipeline.OfferValidator {
    private final DiscountRangeValidatorService delegate;

    @Override
    public void validate(OfferValidationContext ctx) {
        if (ctx.discountType == com.gestion.enums.DiscountType.PERCENTAGE) {
            delegate.validatePercentageNotGreaterThan100(ctx.value);
        }
    }
}