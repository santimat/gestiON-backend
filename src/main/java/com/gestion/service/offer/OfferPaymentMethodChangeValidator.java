package com.gestion.service.offer;

import com.gestion.enums.PaymentMethod;
import com.gestion.service.offer.OfferPaymentMethodsValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class OfferPaymentMethodChangeValidator {
    private final OfferPaymentMethodsValidatorService delegate;

    public void validate(Long offerId, Long commerceId, Set<PaymentMethod> normalizedPaymentMethods) {
        delegate.validateNoAssignedProducts(offerId, commerceId, normalizedPaymentMethods);
    }
}