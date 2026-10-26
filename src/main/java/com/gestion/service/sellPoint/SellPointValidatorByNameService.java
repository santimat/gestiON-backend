package com.gestion.service.sellPoint;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaSellPointRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointValidatorByNameService {
    private final JpaSellPointRepository sellPointRepository;

    public void checkExistingSellPointByName(Long commerceId, String name) {
        if (sellPointRepository.existsByCommerceIdAndNameIgnoreCase(commerceId, name.strip())) {
            throw new DuplicateResourceException(ErrorCode.SELL_POINT_NAME_ALREADY_EXISTS,
                    "Sell point with name '" + name + "' already exists in this commerce");
        }
    }
}
