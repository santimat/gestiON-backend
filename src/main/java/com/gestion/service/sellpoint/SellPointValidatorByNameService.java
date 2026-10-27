package com.gestion.service.sellpoint;

import com.gestion.dto.request.sellpoint.SellPointRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaSellPointRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointValidatorByNameService {
    private final JpaSellPointRepository sellPointRepository;

    public void checkExistingSellPointByName(Long commerceId, SellPointRequest request) {
        if (sellPointRepository.existsByCommerceIdAndNameIgnoreCase(commerceId, request.name().strip())) {
            throw new DuplicateResourceException(ErrorCode.SELL_POINT_NAME_ALREADY_EXISTS,
                    "Sell point with name '" + request.name() + "' already exists in this commerce");
        }
    }
}
