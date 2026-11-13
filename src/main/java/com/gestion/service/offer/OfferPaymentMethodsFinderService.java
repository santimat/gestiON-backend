package com.gestion.service.offer;

import com.gestion.dto.response.offer.OfferPaymentMethodResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OfferPaymentMethodsFinderService {

    private final JpaOfferRepository jpaOfferRepository;

    public List<OfferPaymentMethodResponse> findActivePaymentMethodOffers(Long commerceId) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        List<Offer> offers = jpaOfferRepository
                .findByCommerceIdAndOfferType(commerceId, OfferType.PAYMENT_METHOD, Pageable.unpaged())
                .getContent();

        List<OfferPaymentMethodResponse> result = new ArrayList<>();

        for (Offer offer : offers) {
            if (offer.getPaymentMethods() == null) {
                continue;
            }
            for (PaymentMethod paymentMethod : offer.getPaymentMethods()) {
                result.add(new OfferPaymentMethodResponse(
                        paymentMethod,
                        offer.getValue(),
                        offer.getDiscountType()));
            }
        }

        return result;
    }
}