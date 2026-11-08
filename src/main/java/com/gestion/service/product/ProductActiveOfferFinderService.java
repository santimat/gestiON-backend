package com.gestion.service.product;

import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.service.offer.OfferStatusResolverService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class ProductActiveOfferFinderService {
    private final OfferStatusResolverService offerStatusResolverService;

    // null si el producto no tiene oferta o la que tiene no se puede usar en la venta
    public Offer findActiveProductOffer(Product product, Instant now) {
        Offer offer = product.getOffer();
        if (offer == null || offer.getOfferType() != OfferType.PRODUCT) {
            return null;
        }
        OfferStatus status = offerStatusResolverService.resolveOfferStatus(offer.getStartDate(), offer.getEndDate(), now);
        return status == OfferStatus.ACTIVE ? offer : null;
    }
}
