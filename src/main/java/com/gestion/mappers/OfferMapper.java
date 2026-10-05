package com.gestion.mappers;

import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.model.Offer;

public class OfferMapper {
    public static OfferResponse toResponse(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getProduct().getId(),
                offer.getValue(),
                offer.getStartDate(),
                offer.getEndDate()
        );
    }
}
