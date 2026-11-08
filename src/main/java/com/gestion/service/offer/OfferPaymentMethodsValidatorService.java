package com.gestion.service.offer;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.OfferHasProductsException;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class OfferPaymentMethodsValidatorService {

    private final JpaProductRepository jpaProductRepository;

    public void validateNoAssignedProducts(Long offerId, Long commerceId, Set<PaymentMethod> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return; // ofertas de tipo PRODUCT siempre pueden actualizarse
        }
        if (jpaProductRepository.existsByOfferIdAndCommerceId(offerId, commerceId)) {
            throw new OfferHasProductsException(
                    ErrorCode.OFFER_HAS_PRODUCTS,
                    "This offer has assigned products, so payment methods cannot be applied to it");
        }
    }
}
