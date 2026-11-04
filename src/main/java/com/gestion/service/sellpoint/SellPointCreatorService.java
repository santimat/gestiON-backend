package com.gestion.service.sellpoint;

import com.gestion.dto.request.sellpoint.SellPointRequest;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.mappers.SellPointMapper;
import com.gestion.model.Commerce;
import com.gestion.model.SellPoint;
import com.gestion.repository.JpaSellPointRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointCreatorService {
    private final JpaSellPointRepository sellPointRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final SellPointValidatorByNameService sellPointValidatorByNameService;
    private final SellPointCashiersAssignerService sellPointCashiersAssignerService;

    @Transactional
    public SellPointResponse createSellPoint(SellPointRequest request, Long commerceId) {
        if (commerceId == null) {
            throw new AccessDeniedException("Commerce ID is null");
        }

        SellPointRequest parsedRequest = new SellPointRequest(request.name().strip(), request.cashierIds());
        sellPointValidatorByNameService.checkExistingSellPointByName(commerceId, parsedRequest);

        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        SellPoint savedSellPoint = sellPointRepository.save(SellPointMapper.toEntity(parsedRequest, commerce));

        sellPointCashiersAssignerService.assignCashiers(request.cashierIds(),
                commerceId, savedSellPoint);

        return SellPointMapper.toResponse(savedSellPoint);
    }
}
