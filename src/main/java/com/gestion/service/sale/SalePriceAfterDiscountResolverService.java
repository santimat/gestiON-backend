package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class SalePriceAfterDiscountResolverService {

    private static @NonNull BigDecimal getTotalAfterDiscount(BigDecimal subtotal, DiscountType discountType, BigDecimal discount) {
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
        return totalAfterDiscount;
    }

    public BigDecimal resolvePriceAfterDiscount(BigDecimal subtotal, DiscountType discountType, BigDecimal discount) {
        BigDecimal totalAfterDiscount = getTotalAfterDiscount(subtotal, discountType, discount);

        // la oferta de metodo de pago no puede dejar el total en negativo
        if (totalAfterDiscount.signum() < 0) {
            throw new InvalidDiscountException(ErrorCode.PAYMENT_METHOD_OFFER_EXCEEDS_SALE_SUBTOTAL,
                    "The payment method offer's discount cannot be greater than the sale subtotal");
        }

        return totalAfterDiscount.setScale(2, RoundingMode.HALF_UP);
    }
}
