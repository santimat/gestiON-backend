package com.gestion.service.commerce;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceValidatorByAddressService {
    private final JpaCommerceRepository commerceRepository;

    public void checkExistingCommerceByAddress(String address) {
        if (commerceRepository.existsByAddress(address))
            throw new DuplicateResourceException(ErrorCode.COMMERCE_ADDRESS_ALREADY_EXISTS, "Commerce with address " + address + " already exists");
    }
}
