package com.gestion.service.product;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidProductPriceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class ProductPriceValidatorServiceTests {

    private final ProductPriceValidatorService productPriceValidatorService = new ProductPriceValidatorService();

    @Test
    void validateSalePriceNotLowerThanCostPrice_acceptsSalePriceGreaterThanCostPrice() {
        productPriceValidatorService
                .validateSalePriceNotLowerThanCostPrice(new BigDecimal("100.00"), new BigDecimal("150.00"));
    }

    @Test
    void validateSalePriceNotLowerThanCostPrice_acceptsSalePriceEqualToCostPrice() {
        productPriceValidatorService
                .validateSalePriceNotLowerThanCostPrice(new BigDecimal("100.00"), new BigDecimal("100.00"));
    }

    @Test
    void validateSalePriceNotLowerThanCostPrice_rejectsSalePriceLowerThanCostPrice() {
        InvalidProductPriceException exception = catchThrowableOfType(
                () -> productPriceValidatorService
                        .validateSalePriceNotLowerThanCostPrice(new BigDecimal("200.00"), new BigDecimal("100.00")),
                InvalidProductPriceException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.PRODUCT_SALE_PRICE_BELOW_COST);
    }

    @Test
    void validateSalePriceNotLowerThanCostPrice_rejectsNullCostPrice() {
        InvalidProductPriceException exception = catchThrowableOfType(
                () -> productPriceValidatorService
                        .validateSalePriceNotLowerThanCostPrice(null, new BigDecimal("100.00")),
                InvalidProductPriceException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.INVALID_REQUEST_BODY);
    }
}
