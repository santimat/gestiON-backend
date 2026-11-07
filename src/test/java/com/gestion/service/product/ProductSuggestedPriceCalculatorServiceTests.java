package com.gestion.service.product;

import com.gestion.model.Commerce;
import com.gestion.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductSuggestedPriceCalculatorServiceTests {

    private final ProductSuggestedPriceCalculatorService productSuggestedPriceCalculatorService =
            new ProductSuggestedPriceCalculatorService();

    @Test
    void calculateSuggestedPrice_usesProductMultiplierOverCost() {
        Product product = mock(Product.class);
        when(product.getCostPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.getProfitMultiplier()).thenReturn(1.5);
        Commerce commerce = mock(Commerce.class);

        BigDecimal suggested = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, commerce);

        assertThat(suggested).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void calculateSuggestedPrice_fallsBackToCommerceMultiplierWhenProductMultiplierIsNull() {
        Product product = mock(Product.class);
        when(product.getCostPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.getProfitMultiplier()).thenReturn(null);
        Commerce commerce = mock(Commerce.class);
        when(commerce.getProfitMultiplier()).thenReturn(2.0);

        BigDecimal suggested = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, commerce);

        assertThat(suggested).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void calculateSuggestedPrice_defaultsMultiplierToOnePointZeroWhenBothAreNull() {
        Product product = mock(Product.class);
        when(product.getCostPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.getProfitMultiplier()).thenReturn(null);
        Commerce commerce = mock(Commerce.class);
        when(commerce.getProfitMultiplier()).thenReturn(null);

        BigDecimal suggested = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, commerce);

        assertThat(suggested).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void calculateSuggestedPrice_usesOneWhenCommerceIsNull() {
        Product product = mock(Product.class);
        when(product.getCostPrice()).thenReturn(new BigDecimal("80.00"));
        when(product.getProfitMultiplier()).thenReturn(1.25);

        BigDecimal suggested = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, null);

        assertThat(suggested).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void calculateSuggestedPrice_roundsHalfUp() {
        Product product = mock(Product.class);
        when(product.getCostPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.getProfitMultiplier()).thenReturn(1.3334);

        BigDecimal suggested = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, null);

        assertThat(suggested).isEqualByComparingTo(new BigDecimal("133.34"));
    }
}
