package com.gestion.service.commerce;

import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceValidatorByCuitService {
    private JpaCommerceRepository commerceRepository;

    public void checkExistingCommerceByCuit(String cuit) {
        if (commerceRepository.existsByCuit(cuit))
            throw new DuplicateResourceException("Commerce with cuit " + cuit + " already exists");
    }
}
