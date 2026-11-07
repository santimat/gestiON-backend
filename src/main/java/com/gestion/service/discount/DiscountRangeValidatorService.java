package com.gestion.service.discount;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DiscountRangeValidatorService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");


    public void validatePercentageNotGreaterThan100(BigDecimal percentage) {
        if (percentage == null) {
            throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT, "Discount percentage is required");
        }

        if (percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new InvalidDiscountException(ErrorCode.DISCOUNT_PERCENTAGE_EXCEEDS_100,
                    "Percentage discount cannot be greater than 100");
        }
    }

    public void validateFixedAmountNotGreaterThanTotal(BigDecimal amount, BigDecimal total) {
        if (amount == null || total == null) {
            throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT,
                    "Fixed amount discount and total are required");
        }

        if (amount.compareTo(total) > 0) {
            throw new InvalidDiscountException(ErrorCode.DISCOUNT_EXCEEDS_SUBTOTAL,
                    "Fixed amount discount cannot be greater than the sale subtotal");
        }
    }
}
