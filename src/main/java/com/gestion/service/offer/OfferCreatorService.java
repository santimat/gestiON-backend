package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        LocalDateTime startDate = LocalDateTime.parse(request.startDate());
        LocalDateTime endDate = LocalDateTime.parse(request.endDate());

        //TODO: resolver fecha de inicio y fin para calcular OfferStatus (ACTIVE, EXPIRED, SCHEDULED)
        //TODO: además validar que la fecha de inicio sea menor a la fecha de fin y que no se solapen ofertas para el mismo comercio
        Offer offerToSave = new Offer(null, commerce, request.value(), request.discountType(),
                startDate, endDate, null, request.paymentMethods());
        return OfferMapper.toResponse(offerRepository.save(offerToSave));
    }
}
