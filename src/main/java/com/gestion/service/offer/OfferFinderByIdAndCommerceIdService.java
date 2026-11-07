package com.gestion.service.offer;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OfferFinderByIdAndCommerceIdService {
    private final JpaOfferRepository jpaOfferRepository;

    public Offer findOfferByIdAndCommerceId(Long id, Long commerceId) {
        return jpaOfferRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.OFFER_NOT_FOUND, "Offer with id " + id + " not found"));
    }
}