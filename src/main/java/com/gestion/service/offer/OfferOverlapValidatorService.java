package com.gestion.service.offer;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.OfferOverlapException;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferOverlapValidatorService {

    private final JpaOfferRepository offerRepository;

    public void validateNoOverlap(Long commerceId,
                                 Long currentOfferId,
                                 Instant startDate,
                                 Instant endDate,
                                 BigDecimal value,
                                 DiscountType discountType,
                                 Set<PaymentMethod> paymentMethods) {
        Set<PaymentMethod> normalizedPaymentMethods = paymentMethods == null ? Set.of() : paymentMethods;

        for (Offer existingOffer : offerRepository
                .findOverlappingByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(commerceId, endDate, startDate)) {
            if (currentOfferId != null && currentOfferId.equals(existingOffer.getId())) {
                continue;
            }

            Set<PaymentMethod> existingPaymentMethods = existingOffer.getPaymentMethods() == null
                    ? Set.of()
                    : existingOffer.getPaymentMethods();

            boolean bothNonEmpty = !normalizedPaymentMethods.isEmpty() && !existingPaymentMethods.isEmpty();
            boolean bothEmpty = normalizedPaymentMethods.isEmpty() && existingPaymentMethods.isEmpty();

            if (bothNonEmpty && !Collections.disjoint(normalizedPaymentMethods, existingPaymentMethods)) {
                throw new OfferOverlapException(
                        ErrorCode.OFFER_OVERLAP,
                        "An overlapping offer already exists for the selected payment methods");
            }

            if (bothEmpty
                    && existingOffer.getValue().compareTo(value) == 0
                    && existingOffer.getDiscountType() == discountType) {
                throw new OfferOverlapException(
                        ErrorCode.OFFER_OVERLAP,
                        "An overlapping product offer already exists for the same discount");
            }
        }
    }
}