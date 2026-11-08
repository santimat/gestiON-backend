package com.gestion.service.product;

import com.gestion.enums.DiscountType;
import com.gestion.model.Offer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

// redondeo HALF_UP a 2 decimales, igual que la precision de price en la DB
@Service
@AllArgsConstructor
public class ProductDiscountedPriceCalculatorService {

    public BigDecimal calculateDiscountedPrice(BigDecimal salePrice, Offer offer) {
        BigDecimal value = offer.getValue();
        BigDecimal discounted = offer.getDiscountType() == DiscountType.PERCENTAGE
                ? salePrice.subtract(salePrice.multiply(value).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                : salePrice.subtract(value);
        return discounted.setScale(2, RoundingMode.HALF_UP);
    }
}
