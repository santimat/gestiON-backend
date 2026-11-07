package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.InvalidDiscountException;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.discount.DiscountRangeValidatorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferCreatorServiceTests {

    @Mock
    private JpaOfferRepository offerRepository;

    @Mock
    private CommerceFinderByIdService commerceFinderByIdService;

    @Mock
    private OfferDateValidatorService offerDateValidatorService;

    @Mock
    private OfferStatusResolverService offerStatusResolverService;

    @Spy
    private DiscountRangeValidatorService discountRangeValidatorService = new DiscountRangeValidatorService();

    @InjectMocks
    private OfferCreatorService offerCreatorService;

    @Test
    void createOffer_acceptsPercentageEqualToOneHundred() {
        stubCommerce();
        stubStatus();
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferResponse response = offerCreatorService.createOffer(
                buildRequest(new BigDecimal("100"), DiscountType.PERCENTAGE), 1L);

        assertThat(response.value()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(response.discountType()).isEqualTo(DiscountType.PERCENTAGE);
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void createOffer_rejectsPercentageGreaterThanOneHundred() {
        stubCommerce();

        InvalidDiscountException exception = catchThrowableOfType(
                () -> offerCreatorService.createOffer(
                        buildRequest(new BigDecimal("101"), DiscountType.PERCENTAGE), 1L),
                InvalidDiscountException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(ErrorCode.DISCOUNT_PERCENTAGE_EXCEEDS_100);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void createOffer_acceptsFixedAmountWithoutUpperBound() {
        stubCommerce();
        stubStatus();
        when(offerRepository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferResponse response = offerCreatorService.createOffer(
                buildRequest(new BigDecimal("5000"), DiscountType.FIXED_AMOUNT), 1L);

        assertThat(response.value()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(response.discountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        verify(offerRepository).save(any(Offer.class));
    }

    private void stubCommerce() {
        when(commerceFinderByIdService.findCommerceById(1L)).thenReturn(org.mockito.Mockito.mock(Commerce.class));
    }

    private void stubStatus() {
        when(offerStatusResolverService.resolveOfferStatus(any(), any(), any()))
                .thenReturn(OfferStatus.ACTIVE);
    }

    private OfferRequest buildRequest(BigDecimal value, DiscountType discountType) {
        return new OfferRequest(
                value,
                discountType,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"),
                Set.of(PaymentMethod.CASH)
        );
    }
}
