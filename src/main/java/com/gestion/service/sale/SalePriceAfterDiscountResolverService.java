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

        // red de seguridad: un total negativo nunca debe persistirse.
        // este calculador aplica el descuento de la venta (manual o el de la oferta vigente del
        // metodo de pago) sobre el subtotal; el descuento de la oferta de producto ya se aplico
        // al precio de cada linea antes de armar el subtotal.
        if (totalAfterDiscount.signum() < 0) {
            throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT,
                    "The discount cannot be greater than the sale subtotal");
        }

        return totalAfterDiscount.setScale(2, RoundingMode.HALF_UP);
    }
}
