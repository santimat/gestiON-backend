package com.gestion.mappers;

import com.gestion.dto.request.sellpoint.SellPointRequest;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.model.Commerce;
import com.gestion.model.SellPoint;

import java.util.HashSet;

public class SellPointMapper {

    public static SellPoint toEntity(SellPointRequest request, Commerce commerce) {
        SellPoint sellPoint = new SellPoint();
        sellPoint.setName(request.name());
        sellPoint.setCommerce(commerce);
        sellPoint.setActive(true);
        sellPoint.setUsers(new HashSet<>());
        return sellPoint;
    }

    public static SellPointResponse toResponse(SellPoint sellPoint) {
        return new SellPointResponse(
                sellPoint.getId(),
                sellPoint.getCommerce().getId(),
                sellPoint.getName(),
                sellPoint.isActive(),
                sellPoint.getUpdatedAt()
        );
    }
}
