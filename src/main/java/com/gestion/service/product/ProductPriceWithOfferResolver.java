package com.gestion.service.product;

import com.gestion.model.Offer;
import com.gestion.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class ProductPriceWithOfferResolver {
    private final ProductActiveOfferFinderService offerFinder;
    private final ProductDiscountedPriceCalculatorService priceCalculator;

    public record PriceWithOfferResult(
            BigDecimal discountedPrice,
            Offer activeOffer
    ) {}

    public PriceWithOfferResult resolve(Product product) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Offer activeOffer = offerFinder.findActiveProductOffer(product, now);
        BigDecimal discountedPrice = activeOffer != null
                ? priceCalculator.calculateDiscountedPrice(product.getSalePrice(), activeOffer)
                : product.getSalePrice();
        return new PriceWithOfferResult(discountedPrice, activeOffer);
    }
}