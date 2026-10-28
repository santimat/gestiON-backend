package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Valida la coherencia del descuento de la venta (tipo y monto van juntos, y
 * el monto en rango) y delega el cálculo del precio final al
 * SalePriceAfterDiscountResolverService.
 */
@Service
@AllArgsConstructor
public class SaleDiscountApplierService {
    private final SalePriceAfterDiscountResolverService salePriceAfterDiscountResolverService;

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

        if (discountType == DiscountType.FIXED_AMOUNT && discount.compareTo(subtotal) > 0) {
            throw new InvalidDiscountException(ErrorCode.DISCOUNT_EXCEEDS_SUBTOTAL,
                    "Fixed amount discount cannot be greater than the sale subtotal");
        }

        return salePriceAfterDiscountResolverService
                .resolvePriceAfterDiscount(subtotal, discountType, discount);
    }
}
