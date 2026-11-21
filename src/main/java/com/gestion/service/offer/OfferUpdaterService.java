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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferUpdaterService {

    private final OfferFinderByIdAndCommerceIdService offerFinderByIdAndCommerceIdService;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final OfferDiscountValidator discountValidator;
    private final OfferDateValidatorService dateValidator;
    private final OfferOverlapValidator overlapValidator;
    private final OfferPaymentMethodChangeValidator paymentMethodChangeValidator;
    private final OfferProductPriceValidator productPriceValidator;
    private final OfferStatusResolverService offerStatusResolverService;
    private final JpaOfferRepository offerRepository;

    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferUpdateRequest request, Long commerceId) {
        Offer offer = offerFinderByIdAndCommerceIdService.findOfferByIdAndCommerceId(offerId, commerceId);
        commerceFinderByIdService.findCommerceById(commerceId);

        Instant normalizedStart = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant normalizedEnd = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        discountValidator.validate(request.value(), request.discountType());
        dateValidator.validateOfferDatesForUpdate(normalizedStart, normalizedEnd, now, offer.getStartDate());

        // Set.of() devuelve una coleccion inmutable: Hibernate la reemplaza al mergear la entidad
        // y falla con UnsupportedOperationException al intentar limpiarla.
        Set<PaymentMethod> normalizedPaymentMethods =
                request.paymentMethods() == null ? new HashSet<>() : new HashSet<>(request.paymentMethods());

        paymentMethodChangeValidator.validate(offerId, commerceId, normalizedPaymentMethods);

        overlapValidator.validate(commerceId, offerId, normalizedStart, normalizedEnd,
                request.value(), request.discountType(), request.paymentMethods());

        offer.setValue(request.value());
        offer.setDiscountType(request.discountType());
        offer.setStartDate(normalizedStart);
        offer.setEndDate(normalizedEnd);
        offer.setPaymentMethods(normalizedPaymentMethods);
        offer.setOfferType(normalizedPaymentMethods.isEmpty() ? OfferType.PRODUCT : OfferType.PAYMENT_METHOD);

        // si la oferta ya tiene productos asignados, el nuevo descuento no puede superar el precio
        // de venta de ninguno de ellos.
        productPriceValidator.validate(offerId, commerceId, offer);

        Offer savedOffer = offerRepository.save(offer);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}