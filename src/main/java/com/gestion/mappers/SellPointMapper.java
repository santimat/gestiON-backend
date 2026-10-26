package com.gestion.mappers;

import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.model.Commerce;
import com.gestion.model.SellPoint;
import com.gestion.model.User;
import com.gestion.dto.request.sellPoint.SellPointRequest;

public class SellPointMapper {
    public static SellPoint toEntity(SellPointRequest request, Commerce commerce) {
        if (request == null) {
            return null;
        }
        SellPoint sellPoint = new SellPoint();
        sellPoint.setName(request.name());
        sellPoint.setCommerce(commerce);
        sellPoint.setActive(true);
        return sellPoint;
    }

    public static SellPointResponse toResponse(SellPoint sellPoint) {
        if (sellPoint == null) {
            return null;
        }
        return new SellPointResponse(
                sellPoint.getId(),
                sellPoint.getCommerce() != null ? sellPoint.getCommerce().getId() : null,
                sellPoint.getName(),
                sellPoint.getActive(),
                sellPoint.getCashiers().stream()
                        .map(User::getId)
                        .toList(),
                sellPoint.getCreatedAt()
        );
    }
}
