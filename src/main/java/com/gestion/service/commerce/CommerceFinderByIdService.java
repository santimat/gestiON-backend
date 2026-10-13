package com.gestion.service.commerce;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceFinderByIdService {
    private final JpaCommerceRepository commerceRepository;

    public Commerce findCommerceById(Long id) {
        return commerceRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMMERCE_NOT_FOUND, "Commerce with id " + id + " not found"));
    }
}
