package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferUpdateRequest;
import com.gestion.dto.response.offer.OfferResponse;
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
    private final OfferFinderByIdAndCommerceIdService finder;
    private final CommerceFinderByIdService commerceFinder;
    private final OfferValidationPipeline validationPipeline;
    private final OfferStatusResolverService statusResolver;
    private final JpaOfferRepository repo;

    @Transactional
    public OfferResponse updateOffer(Long offerId, OfferUpdateRequest request, Long commerceId) {
        Offer offer = finder.findOfferByIdAndCommerceId(offerId, commerceId);
        commerceFinder.findCommerceById(commerceId);

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant start = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant end = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Set<PaymentMethod> methods = request.paymentMethods() == null ? new HashSet<>() : new HashSet<>(request.paymentMethods());

        OfferValidationContext ctx = new OfferValidationContext();
        ctx.commerceId = commerceId;
        ctx.offerId = offerId;
        ctx.value = request.value();
        ctx.discountType = request.discountType();
        ctx.startDate = start;
        ctx.endDate = end;
        ctx.paymentMethods = request.paymentMethods() == null ? Set.of() : new HashSet<>(request.paymentMethods());
        ctx.existingOffer = offer;
        ctx.now = now;
        ctx.normalizedPaymentMethods = methods;

        validationPipeline.validate(ctx);

        offer.setValue(request.value());
        offer.setDiscountType(request.discountType());
        offer.setStartDate(start);
        offer.setEndDate(end);
        offer.setPaymentMethods(methods);
        offer.setOfferType(methods.isEmpty() ? OfferType.PRODUCT : OfferType.PAYMENT_METHOD);

        Offer saved = repo.save(offer);
        OfferStatus status = statusResolver.resolveOfferStatus(saved.getStartDate(), saved.getEndDate(), now);
        return OfferMapper.toResponse(saved, status);
    }
}