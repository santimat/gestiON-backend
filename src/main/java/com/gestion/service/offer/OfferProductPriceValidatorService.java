package com.gestion.service.offer;

import com.gestion.enums.DiscountType;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.service.discount.DiscountRangeValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;


@Service
@AllArgsConstructor
public class OfferProductPriceValidatorService {

    private final DiscountRangeValidatorService discountRangeValidatorService;

    public void validateOfferNotGreaterThanProductPrices(Offer offer, Collection<Product> products) {
        if (offer.getDiscountType() != DiscountType.FIXED_AMOUNT) {
            return;
        }

        for (Product product : products) {
            validateOfferNotGreaterThanProductPrice(offer, product);
        }
    }

    public void validateOfferNotGreaterThanProductPrice(Offer offer, Product product) {
        if (offer.getDiscountType() != DiscountType.FIXED_AMOUNT) {
            return;
        }

        validateOfferNotGreaterThanProductPrice(offer, product.getSalePrice());
    }

    public void validateOfferNotGreaterThanProductPrice(Offer offer, BigDecimal salePrice) {
        if (offer.getDiscountType() != DiscountType.FIXED_AMOUNT) {
            return;
        }

        discountRangeValidatorService.validateOfferValueNotGreaterThanProductSalePrice(offer.getValue(), salePrice);
    }
}
