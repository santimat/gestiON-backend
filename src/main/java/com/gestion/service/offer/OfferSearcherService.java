package com.gestion.service.offer;

import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OfferSearcherService {
    private final JpaOfferRepository jpaOfferRepository;
    private final OfferStatusResolverService offerStatusResolverService;
    private final OfferProductCounterService offerProductCounterService;

    @Transactional(readOnly = true)
    public Page<OfferResponse> searchOffers(Long commerceId, OfferStatus status, PaymentMethod paymentMethod, OfferType offerType, Pageable pageable) {
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        Page<Offer> offers = jpaOfferRepository.findAllWithFilters(
                commerceId, status, paymentMethod, offerType, now, pageable);

        List<Long> productOfferIds = offers.getContent().stream()
                .filter(offer -> offer.getOfferType() == OfferType.PRODUCT)
                .map(Offer::getId)
                .toList();

        Map<Long, Long> productCountsByOfferId = offerProductCounterService
                .countProductsByOfferIds(commerceId, productOfferIds);

        return offers.map(offer -> OfferMapper.toResponse(
                offer,
                offerStatusResolverService.resolveOfferStatus(offer.getStartDate(), offer.getEndDate(), now),
                productCountsByOfferId.getOrDefault(offer.getId(), 0L)));
    }
}
