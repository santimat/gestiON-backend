package com.gestion.service.offer;

import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OfferSearcherService {
    private final JpaOfferRepository offerRepository;

    public Page<OfferResponse> searchOffers(Pageable pageable, Long commerceId) {
        Page<Offer> offers = offerRepository.findAllByCommerceId(commerceId, pageable);
        return offers.map(OfferMapper::toResponse);
    }
}
