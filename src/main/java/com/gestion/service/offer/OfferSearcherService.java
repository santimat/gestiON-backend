package com.gestion.service.offer;

import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
public class OfferSearcherService {
    private final JpaOfferRepository jpaOfferRepository;
    private final OfferStatusResolverService offerStatusResolverService;

    @Transactional(readOnly = true)
    public Page<OfferResponse> searchOffers(Long commerceId, OfferStatus status, Pageable pageable) {
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        Page<Offer> offers = jpaOfferRepository.findAllWithFilters(
                commerceId, status, now, pageable);

        return offers.map(offer -> OfferMapper.toResponse(offer,
                offerStatusResolverService.resolveOfferStatus(offer.getStartDate(), offer.getEndDate(), now)));
    }
}
