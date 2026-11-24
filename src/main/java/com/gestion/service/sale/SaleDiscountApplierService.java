package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleDiscountApplierService {

    private final SalePriceAfterDiscountResolverService salePriceAfterDiscountResolverService;

    public BigDecimal applyDiscount(BigDecimal subtotal, DiscountType discountType, BigDecimal discount) {
        if (discountType == null || discount == null) {
            return subtotal;
        }

        return salePriceAfterDiscountResolverService
                .resolvePriceAfterDiscount(subtotal, discountType, discount);
    }
}
