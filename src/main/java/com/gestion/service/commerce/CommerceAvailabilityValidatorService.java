package com.gestion.service.commerce;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.CommerceNotAvailableException;
import org.springframework.stereotype.Service;

@Service
public class CommerceAvailabilityValidatorService {

    public void checkCommerceIsAvailable(Long commerceId) {
        if (commerceId == null) {
            throw new CommerceNotAvailableException(ErrorCode.COMMERCE_NOT_AVAILABLE,
                    ErrorCode.COMMERCE_NOT_AVAILABLE.getDefaultMessage());
        }
    }
}
