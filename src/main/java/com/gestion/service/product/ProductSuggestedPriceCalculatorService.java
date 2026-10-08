package com.gestion.service.product;

import com.gestion.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductSuggestedPriceCalculatorService {

    public BigDecimal calculateSuggestedPrice(Product product) {
        Double profitMultiplier = product.getProfitMultiplier();

        if (profitMultiplier == null && product.getCommerce() != null) {
            profitMultiplier = product.getCommerce().getProfitMultiplier();
        }

        if (profitMultiplier == null) {
            profitMultiplier = 1.0;
        }

        return product.getSalePrice()
                .multiply(BigDecimal.valueOf(profitMultiplier))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
