package com.gestion.service.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.service.offer.OfferOverlapValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferOverlapValidator {
    private final OfferOverlapValidatorService delegate;

    public void validate(Long commerceId, Long offerId, Instant startDate, Instant endDate,
                         BigDecimal value, DiscountType discountType, Set<PaymentMethod> paymentMethods) {
        delegate.validateNoOverlap(commerceId, offerId, startDate, endDate, value, discountType, paymentMethods);
    }
}