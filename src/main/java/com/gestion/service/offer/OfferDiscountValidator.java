package com.gestion.service.offer;

import com.gestion.enums.DiscountType;
import com.gestion.service.discount.DiscountRangeValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class OfferDiscountValidator {
    private final DiscountRangeValidatorService delegate;

    public void validate(BigDecimal value, DiscountType discountType) {
        if (discountType == DiscountType.PERCENTAGE) {
            delegate.validatePercentageNotGreaterThan100(value);
        }
    }
}