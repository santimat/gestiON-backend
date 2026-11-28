package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.model.SellPoint;
import com.gestion.service.sellpoint.SellPointFinderByIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SaleSellPointResolverService {
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;

    public SellPoint resolveSellPoint(Long sellPointId, Long commerceId) {
        if (sellPointId == null) {
            return null;
        }

        SellPoint sellPoint = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, commerceId);

        if (!sellPoint.isActive()) {
            throw new InactiveResourceException(ErrorCode.SELL_POINT_INACTIVE,
                    "Sell point '" + sellPoint.getName() + "' is not active");
        }
        return sellPoint;
    }
}
