package com.gestion.service.offer;

import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OfferFinderByPaymentMethodAndCommerceIdService {

    private final JpaOfferRepository jpaOfferRepository;

    @Transactional()
    public Optional<Offer> findActiveByPaymentMethodAndCommerceId(PaymentMethod paymentMethod, Long commerceId) {
        if (paymentMethod == null || commerceId == null) {
            return Optional.empty();
        }

        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        Page<Offer> offers = jpaOfferRepository.findAllWithFilters(
                commerceId, OfferStatus.ACTIVE, paymentMethod, OfferType.PAYMENT_METHOD, now, Pageable.unpaged());

        return offers.getContent().stream().findFirst();
    }
}
