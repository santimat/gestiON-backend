package com.gestion.service.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.mappers.SellPointMapper;
import com.gestion.model.SellPoint;
import com.gestion.repository.JpaSellPointRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SellPointSearcherService {
    private final JpaSellPointRepository sellPointRepository;

    @Transactional(readOnly = true)
    public Page<SellPointResponse> searchSellPoints(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<SellPoint> sellPoints = sellPointRepository
                .findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);
        return sellPoints.map(SellPointMapper::toResponse);
    }
}
