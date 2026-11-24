package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.DiscountType;
import com.gestion.enums.OfferStatus;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.discount.DiscountRangeValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final OfferDateValidatorService dateValidator;
    private final OfferOverlapValidatorService overlapValidator;
    private final OfferStatusResolverService offerStatusResolverService;
    private final DiscountRangeValidatorService discountRangeValidatorService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        Instant normalizedStart = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant normalizedEnd = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        if(request.discountType() == DiscountType.PERCENTAGE){
            discountRangeValidatorService.validatePercentageNotGreaterThan100(request.value());
        }

        dateValidator.validateOfferDates(normalizedStart, normalizedEnd, now);

        overlapValidator.validateNoOverlap(commerceId, null, normalizedStart, normalizedEnd,
                request.value(), request.discountType(), request.paymentMethods());

        OfferRequest normalizedRequest = new OfferRequest(
                request.value(),
                request.discountType(),
                normalizedStart,
                normalizedEnd,
                request.paymentMethods()
        );

        Offer offerToSave = OfferMapper.toEntity(normalizedRequest, commerce);
        Offer savedOffer = offerRepository.save(offerToSave);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus, 0L);
    }
}