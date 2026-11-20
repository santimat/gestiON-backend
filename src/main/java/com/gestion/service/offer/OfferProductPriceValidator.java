package com.gestion.service.offer;

import com.gestion.model.Product;
import com.gestion.service.product.ProductFinderByOfferIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Order(5)
@AllArgsConstructor
public class OfferProductPriceValidator implements OfferValidationPipeline.OfferValidator {
    private final OfferProductPriceValidatorService delegate;
    private final ProductFinderByOfferIdAndCommerceIdService productFinder;

    @Override
    public void validate(OfferValidationContext ctx) {
        if (ctx.offerId != null) {
            List<Product> products = productFinder.findProductsByOfferIdAndCommerceId(ctx.offerId, ctx.commerceId);
            delegate.validateOfferNotGreaterThanProductPrices(ctx.existingOffer, products);
        }
    }
}