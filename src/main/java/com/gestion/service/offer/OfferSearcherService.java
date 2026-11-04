package com.gestion.service.offer;

import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OfferSearcherService {
    private final JpaOfferRepository jpaOfferRepository;

    @Transactional(readOnly = true)
    public Page<OfferResponse> searchOffers(Long commerceId, Boolean expired, Pageable pageable) {
        Page<Offer> offers = jpaOfferRepository.findAllWithFilters(
                commerceId, expired, pageable);

        return offers.map(OfferMapper::toResponse);
    }
}
