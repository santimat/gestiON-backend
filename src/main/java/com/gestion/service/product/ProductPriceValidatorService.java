package com.gestion.service.product;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidProductPriceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductPriceValidatorService {

    public void validateSalePriceNotLowerThanCostPrice(BigDecimal costPrice, BigDecimal salePrice) {
        if (salePrice.compareTo(costPrice) < 0) {
            throw new InvalidProductPriceException(ErrorCode.PRODUCT_SALE_PRICE_BELOW_COST,
                    "Sale price cannot be lower than cost price");
        }
    }
}
