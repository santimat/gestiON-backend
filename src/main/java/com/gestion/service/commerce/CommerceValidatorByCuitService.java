package com.gestion.service.commerce;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceValidatorByCuitService {
    private final JpaCommerceRepository commerceRepository;

    public void checkExistingCommerceByCuit(String cuit) {
        if (commerceRepository.existsByCuit(cuit))
            throw new DuplicateResourceException(ErrorCode.COMMERCE_CUIT_ALREADY_EXISTS, "Commerce with cuit " + cuit + " already exists");
    }
}
