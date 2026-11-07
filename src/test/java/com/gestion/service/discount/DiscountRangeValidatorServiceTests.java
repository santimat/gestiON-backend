package com.gestion.service.discount;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class DiscountRangeValidatorServiceTests {

    private final DiscountRangeValidatorService discountRangeValidatorService = new DiscountRangeValidatorService();

    @Test
    void validatePercentageNotGreaterThan100_acceptsValueBelowOneHundred() {
        assertThatCode(() -> discountRangeValidatorService
                .validatePercentageNotGreaterThan100(new BigDecimal("10"))).doesNotThrowAnyException();
    }

    @Test
    void validatePercentageNotGreaterThan100_acceptsExactlyOneHundred() {
        assertThatCode(() -> discountRangeValidatorService
                .validatePercentageNotGreaterThan100(new BigDecimal("100"))).doesNotThrowAnyException();
    }

    @Test
    void validatePercentageNotGreaterThan100_rejectsValueGreaterThanOneHundred() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> discountRangeValidatorService.validatePercentageNotGreaterThan100(new BigDecimal("100.01")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_PERCENTAGE_EXCEEDS_100);
    }

    @Test
    void validatePercentageNotGreaterThan100_rejectsNullValue() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> discountRangeValidatorService.validatePercentageNotGreaterThan100(null),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_DISCOUNT);
    }

    @Test
    void validateFixedAmountNotGreaterThanTotal_acceptsAmountLowerThanTotal() {
        assertThatCode(() -> discountRangeValidatorService
                .validateFixedAmountNotGreaterThanTotal(new BigDecimal("500"), new BigDecimal("1000")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFixedAmountNotGreaterThanTotal_acceptsAmountEqualToTotal() {
        assertThatCode(() -> discountRangeValidatorService
                .validateFixedAmountNotGreaterThanTotal(new BigDecimal("1000"), new BigDecimal("1000")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFixedAmountNotGreaterThanTotal_rejectsAmountGreaterThanTotal() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> discountRangeValidatorService
                        .validateFixedAmountNotGreaterThanTotal(new BigDecimal("1000.01"), new BigDecimal("1000")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_EXCEEDS_SUBTOTAL);
    }

    @Test
    void validateFixedAmountNotGreaterThanTotal_rejectsNullValues() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> discountRangeValidatorService.validateFixedAmountNotGreaterThanTotal(null, new BigDecimal("1000")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_DISCOUNT);
    }
}
