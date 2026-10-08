package com.gestion.service.product;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductSuggestedPriceCalculatorService {

    public BigDecimal calculateSuggestedPrice(BigDecimal costPrice, Double profitMultiplier) {
        return costPrice.multiply(BigDecimal.valueOf(profitMultiplier));
    }
}
