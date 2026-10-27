package com.gestion.service.sellpoint;

import com.gestion.config.UserPrincipal;
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

    @Transactional
    public SellPointResponse createSellPoint(SellPointRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user or commerce ID is null");
        }

        request = new SellPointRequest(request.name().strip());
        sellPointValidatorByNameService.checkExistingSellPointByName(authenticatedUser.getCommerceId(), request);

        Commerce commerce = commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId());
        SellPoint newSellPoint = sellPointRepository.save(SellPointMapper.toEntity(request, commerce));
        return SellPointMapper.toResponse(newSellPoint);
    }
}
