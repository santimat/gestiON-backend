package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import com.gestion.service.discount.DiscountRangeValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleDiscountApplierService {
    private final SalePriceAfterDiscountResolverService salePriceAfterDiscountResolverService;
    private final DiscountRangeValidatorService discountRangeValidatorService;

    public BigDecimal applyDiscount(BigDecimal subtotal, DiscountType discountType, BigDecimal discount) {
        if (discountType == null) {
            if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                throw new InvalidDiscountException(ErrorCode.DISCOUNT_WITHOUT_TYPE,
                        "A discount amount was provided without a discount type");
            }
            return subtotal;
        }

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDiscountException(ErrorCode.TYPE_WITHOUT_DISCOUNT,
                    "A discount type was provided without a discount amount");
        }

        if (discountType == DiscountType.FIXED_AMOUNT) {
            discountRangeValidatorService.validateFixedAmountNotGreaterThanTotal(discount, subtotal);
        } else if (discountType == DiscountType.PERCENTAGE) {
            discountRangeValidatorService.validatePercentageNotGreaterThan100(discount);
        }

        return salePriceAfterDiscountResolverService
                .resolvePriceAfterDiscount(subtotal, discountType, discount);
    }
}
