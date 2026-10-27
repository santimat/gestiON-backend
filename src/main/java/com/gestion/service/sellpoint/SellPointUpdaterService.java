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
    private final SellPointCashiersLoaderService sellPointCashiersLoaderService;
    private final JpaSellPointRepository sellPointRepository;

    @Transactional
    public SellPointResponse updateSellPoint(Long sellPointId, SellPointUpdateRequest request, Long commerceId) {
        SellPoint sellPointToUpdate = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, commerceId);

        SellPointUpdateRequest parsedRequest = new SellPointUpdateRequest(request.name().strip(), request.cashierIds());
        sellPointValidatorByNameService.checkExistingSellPointByName(commerceId, sellPointId, parsedRequest);

        sellPointToUpdate.setName(parsedRequest.name());
        sellPointToUpdate.getUsers().clear();
        sellPointToUpdate.getUsers().addAll(sellPointCashiersLoaderService
                .loadCashiers(parsedRequest.cashierIds(), commerceId));

        SellPoint updatedSellPoint = sellPointRepository.save(sellPointToUpdate);
        return SellPointMapper.toResponse(updatedSellPoint);
    }
}
