package com.gestion.service.sale;

import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import com.gestion.service.discount.DiscountRangeValidatorService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class SaleDiscountApplierServiceTests {

    private static final BigDecimal SUBTOTAL = new BigDecimal("1000");

    private final SaleDiscountApplierService saleDiscountApplierService = new SaleDiscountApplierService(
            new SalePriceAfterDiscountResolverService(),
            new DiscountRangeValidatorService());

    @Test
    void applyDiscount_appliesPercentage() {
        BigDecimal total = saleDiscountApplierService
                .applyDiscount(SUBTOTAL, DiscountType.PERCENTAGE, new BigDecimal("10"));

        assertThat(total).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    void applyDiscount_acceptsExactlyOneHundredPercent() {
        BigDecimal total = saleDiscountApplierService
                .applyDiscount(SUBTOTAL, DiscountType.PERCENTAGE, new BigDecimal("100"));

        assertThat(total).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void applyDiscount_rejectsPercentageGreaterThanOneHundred() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> saleDiscountApplierService
                        .applyDiscount(SUBTOTAL, DiscountType.PERCENTAGE, new BigDecimal("100.01")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_PERCENTAGE_EXCEEDS_100);
    }

    @Test
    void applyDiscount_appliesFixedAmount() {
        BigDecimal total = saleDiscountApplierService
                .applyDiscount(SUBTOTAL, DiscountType.FIXED_AMOUNT, new BigDecimal("250"));

        assertThat(total).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    void applyDiscount_acceptsFixedAmountEqualToSubtotal() {
        BigDecimal total = saleDiscountApplierService
                .applyDiscount(SUBTOTAL, DiscountType.FIXED_AMOUNT, new BigDecimal("1000"));

        assertThat(total).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void applyDiscount_rejectsFixedAmountGreaterThanSubtotal() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> saleDiscountApplierService
                        .applyDiscount(SUBTOTAL, DiscountType.FIXED_AMOUNT, new BigDecimal("1000.01")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_EXCEEDS_SUBTOTAL);
    }

    @Test
    void applyDiscount_withoutTypeAndWithoutAmount_keepsSubtotal() {
        BigDecimal total = saleDiscountApplierService.applyDiscount(SUBTOTAL, null, null);

        assertThat(total).isEqualByComparingTo(SUBTOTAL);
    }

    @Test
    void applyDiscount_withoutType_rejectsAmount() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> saleDiscountApplierService.applyDiscount(SUBTOTAL, null, new BigDecimal("10")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_WITHOUT_TYPE);
    }

    @Test
    void applyDiscount_withoutAmount_rejectsType() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> saleDiscountApplierService.applyDiscount(SUBTOTAL, DiscountType.PERCENTAGE, null),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.TYPE_WITHOUT_DISCOUNT);
    }

    @Test
    void applyDiscount_withZeroAmount_rejectsType() {
        InvalidDiscountException exception = catchThrowableOfType(
                () -> saleDiscountApplierService.applyDiscount(SUBTOTAL, DiscountType.PERCENTAGE, BigDecimal.ZERO),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.TYPE_WITHOUT_DISCOUNT);
    }

    @Test
    void resolvePriceAfterDiscount_neverReturnsNegativeTotal() {
        SalePriceAfterDiscountResolverService resolver = new SalePriceAfterDiscountResolverService();

        InvalidDiscountException exception = catchThrowableOfType(
                () -> resolver.resolvePriceAfterDiscount(SUBTOTAL, DiscountType.PERCENTAGE, new BigDecimal("150")),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_DISCOUNT);
    }
}
