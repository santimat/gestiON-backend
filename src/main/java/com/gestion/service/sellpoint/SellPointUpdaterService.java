package com.gestion.service.sellpoint;

import com.gestion.dto.request.sellpoint.SellPointUpdateRequest;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.mappers.SellPointMapper;
import com.gestion.model.SellPoint;
import com.gestion.repository.JpaSellPointRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointUpdaterService {
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;
    private final SellPointValidatorByNameService sellPointValidatorByNameService;
    private final SellPointCashiersAssignerService sellPointCashiersAssignerService;
    private final JpaSellPointRepository sellPointRepository;

    @Transactional
    public SellPointResponse updateSellPoint(Long sellPointId, SellPointUpdateRequest request, Long commerceId) {
        SellPoint sellPointToUpdate = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, commerceId);

        SellPointUpdateRequest parsedRequest = new SellPointUpdateRequest(request.name().strip(), request.cashierIds());
        sellPointValidatorByNameService.checkExistingSellPointByName(commerceId, sellPointId, parsedRequest);

        sellPointToUpdate.setName(parsedRequest.name());
        sellPointToUpdate.getUsers().forEach(user -> user.setSellPoint(null));
        sellPointCashiersAssignerService.assignCashiers(parsedRequest.cashierIds(), commerceId, sellPointToUpdate);

        SellPoint updatedSellPoint = sellPointRepository.save(sellPointToUpdate);
        return SellPointMapper.toResponse(updatedSellPoint);
    }
}
