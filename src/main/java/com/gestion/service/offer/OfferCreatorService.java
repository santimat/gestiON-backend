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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final OfferDateValidatorService offerDateValidatorService;
    private final OfferStatusResolverService offerStatusResolverService;
    private final DiscountRangeValidatorService discountRangeValidatorService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        // el valor de la oferta depende del tipo: en porcentaje no puede pasar el 100%.
        // en monto fijo no se valida un máximo porque todavía no existe contra qué total
        // compararlo (las ofertas aún no se aplican a la venta).
        if (request.discountType() == DiscountType.PERCENTAGE) {
            discountRangeValidatorService.validatePercentageNotGreaterThan100(request.value());
        }

        Instant normalizedStart = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant normalizedEnd = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        OfferRequest normalizedRequest = new OfferRequest(
                request.value(),
                request.discountType(),
                normalizedStart,
                normalizedEnd,
                request.paymentMethods()
        );

        offerDateValidatorService.validateOfferDates(normalizedStart, normalizedEnd, now);

        Offer offerToSave = OfferMapper.toEntity(normalizedRequest, commerce);
        Offer savedOffer = offerRepository.save(offerToSave);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}
