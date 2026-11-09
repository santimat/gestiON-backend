package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleDetailRequest;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class SaleCreatorIntegrationTests {

    @Autowired
    private SaleCreatorService saleCreatorService;

    @Autowired
    private JpaUserRepository userRepository;

    private User santino() {
        return userRepository.findByEmail("santino@gmail.com")
                .orElseThrow(() -> new IllegalStateException("santino not seeded"));
    }

    @Test
    void createSale_withPaymentMethodOffer_appliesProductAndPaymentDiscounts() {
        // producto 1 (Coca Cola) tiene la oferta 8 (25% PRICE) y el metodo de pago
        // CASH tiene la oferta 7 (15% PAYMENT_METHOD).
        // sale price original = 1200; con oferta de product = 900; con oferta de pago
        // CASH (15%) => subtotal 900, total 765.
        SaleRequest request = new SaleRequest(
                PaymentMethod.CASH,
                Set.of(new SaleDetailRequest(1L, 1)),
                null,
                null,   // discountType -> el offer de pago sobrescribe
                null,   // discount -> sobrescrito por la offer
                null
        );

        SaleSummaryResponse response = saleCreatorService.createSale(
                request, santino().getId(), 1L, santino().getSellPoint() == null ? null : santino().getSellPoint().getId());

        assertEquals(0, response.subtotal().compareTo(new BigDecimal("900.00")));
        // discountType y discount vienen del offer de payment method (15%)
        assertEquals(DiscountType.PERCENTAGE, response.discountType());
        assertEquals(0, response.discount().compareTo(new BigDecimal("15.00")));
        assertEquals(0, response.total().compareTo(new BigDecimal("765.00")));
    }

    @Test
    void createSale_withProductOffer_only_whenNoPaymentMethodOffer() {
        // TRANSFER no tiene offer de payment method; producto 1 tiene la offer (25%).
        // total = 900, no hay descuento de venta (null).
        SaleRequest request = new SaleRequest(
                PaymentMethod.TRANSFER,
                Set.of(new SaleDetailRequest(1L, 1)),
                null,
                null,
                null,
                null
        );

        SaleSummaryResponse response = saleCreatorService.createSale(
                request, santino().getId(), 1L, santino().getSellPoint() == null ? null : santino().getSellPoint().getId());

        assertEquals(0, response.subtotal().compareTo(new BigDecimal("900.00")));
        assertEquals(null, response.discountType());
        assertEquals(0, response.total().compareTo(new BigDecimal("900.00")));
    }

    @Test
    void createSale_withoutAnyOffer_chargesSalePrice() {
        // producto 2 (Agua Mineral) no tiene offer; metodo de pago CREDIT_ACCOUNT no
        // tiene ninguna offer vigente -> el precio final es el sale price.
        SaleRequest request = new SaleRequest(
                PaymentMethod.CREDIT_ACCOUNT,
                Set.of(new SaleDetailRequest(2L, 1)),
                null,
                null,
                null,
                null
        );

        SaleSummaryResponse response = saleCreatorService.createSale(
                request, santino().getId(), 1L, santino().getSellPoint() == null ? null : santino().getSellPoint().getId());

        assertEquals(0, response.subtotal().compareTo(new BigDecimal("900.00")));
        assertEquals(null, response.discountType());
        assertEquals(0, response.total().compareTo(new BigDecimal("900.00")));
    }
}
