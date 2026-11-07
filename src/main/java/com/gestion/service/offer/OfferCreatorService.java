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

        Set<PaymentMethod> normalizedPaymentMethods =
                request.paymentMethods() == null ? Set.of() : request.paymentMethods();

        // ofertas con el mismo rango de fechas
        List<Offer> existingOffers = offerRepository.findOverlappingByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                commerceId, normalizedEnd, normalizedStart);

        for (Offer existingOffer : existingOffers) {

            // evitamos nullPointerException
            Set<PaymentMethod> existingPaymentMethods =
                    existingOffer.getPaymentMethods() == null
                            ? Set.of()
                            : existingOffer.getPaymentMethods();


            boolean bothNonEmpty = !normalizedPaymentMethods.isEmpty() && !existingPaymentMethods.isEmpty();
            boolean bothEmpty = normalizedPaymentMethods.isEmpty() && existingPaymentMethods.isEmpty();

            if (bothNonEmpty &&
                    // comparten al menos un método de pago
                    !Collections.disjoint(normalizedPaymentMethods, existingPaymentMethods)) {
                throw new OfferOverlapException(
                        ErrorCode.OFFER_OVERLAP,
                        "An overlapping offer already exists for the selected payment methods");
            }

            // si ambos metodos de pago están vacíos, significa que la oferta se usa para productos.
            // en ese caso, no se permite que existan dos ofertas de productos con el mismo valor y tipo de descuento.
            if (bothEmpty
                    && existingOffer.getValue().compareTo(normalizedRequest.value()) == 0
                    && existingOffer.getDiscountType() == normalizedRequest.discountType()) {
                throw new OfferOverlapException(
                        ErrorCode.OFFER_OVERLAP,
                        "An overlapping product offer already exists for the same discount");
            }
        }

        Offer offerToSave = OfferMapper.toEntity(normalizedRequest, commerce);
        Offer savedOffer = offerRepository.save(offerToSave);

        OfferStatus offerStatus = offerStatusResolverService.resolveOfferStatus(
                savedOffer.getStartDate(), savedOffer.getEndDate(), now);

        return OfferMapper.toResponse(savedOffer, offerStatus);
    }
}
