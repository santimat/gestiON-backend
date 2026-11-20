package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.PaymentMethod;
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
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final CommerceFinderByIdService commerceFinder;
    private final OfferValidationPipeline validationPipeline;
    private final OfferStatusResolverService statusResolver;
    private final JpaOfferRepository repo;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinder.findCommerceById(commerceId);

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant start = request.startDate().truncatedTo(ChronoUnit.MICROS);
        Instant end = request.endDate().truncatedTo(ChronoUnit.MICROS);
        Set<PaymentMethod> methods = request.paymentMethods() == null ? Set.of() : new HashSet<>(request.paymentMethods());

        OfferValidationContext ctx = new OfferValidationContext();
        ctx.commerceId = commerceId;
        ctx.offerId = null;
        ctx.value = request.value();
        ctx.discountType = request.discountType();
        ctx.startDate = start;
        ctx.endDate = end;
        ctx.paymentMethods = methods;
        ctx.now = now;
        ctx.normalizedPaymentMethods = methods;

        validationPipeline.validate(ctx);

        Offer offer = OfferMapper.toEntity(request, commerce);
        Offer saved = repo.save(offer);
        OfferStatus status = statusResolver.resolveOfferStatus(saved.getStartDate(), saved.getEndDate(), now);
        return OfferMapper.toResponse(saved, status);
    }
}