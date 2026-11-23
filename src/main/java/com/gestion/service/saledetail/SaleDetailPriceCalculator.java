package com.gestion.service.saledetail;

import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import com.gestion.service.product.ProductActiveOfferFinderService;
import com.gestion.service.product.ProductDiscountedPriceCalculatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class SaleDetailPriceCalculator {
    private final ProductActiveOfferFinderService offerFinder;
    private final ProductDiscountedPriceCalculatorService priceCalculator;

    public record PriceCalculationResult(
            BigDecimal unitPrice,
            Offer activeOffer
    ) {}

    public PriceCalculationResult calculatePrice(Product product) {
        // única fuente de verdad del "ahora" para decidir qué ofertas están vigentes
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        Offer activeOffer = offerFinder.findActiveProductOffer(product, now);
        BigDecimal unitPrice = (activeOffer != null)
                ? priceCalculator.calculateDiscountedPrice(product.getSalePrice(), activeOffer)
                : product.getSalePrice();

        return new PriceCalculationResult(unitPrice, activeOffer);
    }

    public void applyOfferSnapshot(SaleDetail detail, Offer activeOffer) {
        if (activeOffer != null) {
            detail.setDiscountApplied(activeOffer.getValue());
            detail.setDiscountTypeApplied(activeOffer.getDiscountType());
        }
    }
}