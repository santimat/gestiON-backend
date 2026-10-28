package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class SalePriceAfterDiscountResolverService {

    public BigDecimal resolvePriceAfterDiscount(BigDecimal subtotal, DiscountType discountType, BigDecimal discount) {
        BigDecimal totalAfterDiscount;
        switch (discountType) {
            case PERCENTAGE -> {
                BigDecimal hundred = BigDecimal.valueOf(100);
                totalAfterDiscount = subtotal.subtract(
                        subtotal.multiply(discount).divide(hundred, 2, RoundingMode.HALF_UP));
            }
            case FIXED_AMOUNT -> totalAfterDiscount = subtotal.subtract(discount);
            default -> throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT,
                    "Unsupported discount type");
        }

        return totalAfterDiscount.setScale(2, RoundingMode.HALF_UP);
    }
}
