package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.DiscountType;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.PaymentMethod;
import com.gestion.exception.OfferOverlapException;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final OfferDateValidatorService offerDateValidatorService;
    private final OfferOverlapValidatorService offerOverlapValidatorService;
    private final OfferStatusResolverService offerStatusResolverService;
    private final DiscountRangeValidatorService discountRangeValidatorService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        // el valor de la oferta depende del tipo: en porcentaje no puede pasar el 100%.
        // en monto fijo no se valida un máximo acá porque la oferta se crea todavía sin
        // productos asociados: no hay contra qué salePrice compararlo. el capeo contra el
        // precio de venta de cada producto se hace al asignarle los productos
        // (OfferAssignerProductService) y también al actualizar el valor de una oferta que
        // ya tiene productos (OfferUpdaterService).
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

        offerOverlapValidatorService.validateNoOverlap(
                commerceId,
                null,
                normalizedStart,
                normalizedEnd,
                request.value(),
                request.discountType(),
                request.paymentMethods());

        Offer offerToSave = OfferMapper.toEntity(normalizedRequest, commerce);
        Offer savedOffer = offerRepository.save(offerToSave);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}
