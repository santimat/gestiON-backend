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
    private final OfferDiscountValidator discountValidator;
    private final OfferDateValidator dateValidator;
    private final OfferOverlapValidator overlapValidator;
    private final OfferStatusResolverService offerStatusResolverService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        Instant normalizedStart = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant normalizedEnd = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        discountValidator.validate(request.value(), request.discountType());
        dateValidator.validateForCreate(normalizedStart, normalizedEnd, now);
        overlapValidator.validate(commerceId, null, normalizedStart, normalizedEnd,
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

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}