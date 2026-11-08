package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferUpdateRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.DiscountType;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.discount.DiscountRangeValidatorService;
import com.gestion.service.product.ProductFinderByOfferIdAndCommerceIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferUpdaterService {

    private final OfferFinderByIdAndCommerceIdService offerFinderByIdAndCommerceIdService;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final OfferDateValidatorService offerDateValidatorService;
    private final OfferStatusResolverService offerStatusResolverService;
    private final DiscountRangeValidatorService discountRangeValidatorService;
    private final OfferOverlapValidatorService offerOverlapValidatorService;
    private final OfferPaymentMethodsValidatorService offerPaymentMethodsValidatorService;
    private final JpaOfferRepository offerRepository;
    private final ProductFinderByOfferIdAndCommerceIdService productFinderByOfferIdAndCommerceIdService;

    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferUpdateRequest request, Long commerceId) {
        Offer offer = offerFinderByIdAndCommerceIdService.findOfferByIdAndCommerceId(offerId, commerceId);
        commerceFinderByIdService.findCommerceById(commerceId);

        if (request.discountType() == DiscountType.PERCENTAGE) {
            discountRangeValidatorService.validatePercentageNotGreaterThan100(request.value());
        }

        Instant normalizedStart = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant normalizedEnd = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        offerDateValidatorService.validateOfferDatesForUpdate(normalizedStart, normalizedEnd, now, offer.getStartDate());

        Set<PaymentMethod> normalizedPaymentMethods =
                request.paymentMethods() == null ? Set.of() : request.paymentMethods();

        offerPaymentMethodsValidatorService.validateNoAssignedProducts(offerId, commerceId, normalizedPaymentMethods);

        offerOverlapValidatorService.validateNoOverlap(
                commerceId,
                offerId,
                normalizedStart,
                normalizedEnd,
                request.value(),
                request.discountType(),
                request.paymentMethods());

        offer.setValue(request.value());
        offer.setDiscountType(request.discountType());
        offer.setStartDate(normalizedStart);
        offer.setEndDate(normalizedEnd);
        offer.setPaymentMethods(normalizedPaymentMethods);
        offer.setOfferType(normalizedPaymentMethods.isEmpty() ? OfferType.PRODUCT : OfferType.PAYMENT_METHOD);

        Offer savedOffer = offerRepository.save(offer);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}