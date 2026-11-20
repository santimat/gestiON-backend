package com.gestion.service.offer;

import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.service.offer.OfferProductPriceValidatorService;
import com.gestion.service.product.ProductFinderByOfferIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OfferProductPriceValidator {
    private final OfferProductPriceValidatorService delegate;
    private final ProductFinderByOfferIdAndCommerceIdService productFinder;

    public void validate(Long offerId, Long commerceId, Offer offer) {
        List<Product> assignedProducts = productFinder.findProductsByOfferIdAndCommerceId(offerId, commerceId);
        delegate.validateOfferNotGreaterThanProductPrices(offer, assignedProducts);
    }
}