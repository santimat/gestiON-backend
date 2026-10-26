package com.gestion.service.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sellPoint.SellPointRequest;
import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.mappers.SellPointMapper;
import com.gestion.model.Commerce;
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
        sellPointValidatorByNameService.checkExistingSellPointByName(authenticatedUser.getCommerceId(), request.name());

        Commerce commerce = commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId());
        return SellPointMapper.toResponse(sellPointRepository.save(SellPointMapper.toEntity(request, commerce)));
    }
}
