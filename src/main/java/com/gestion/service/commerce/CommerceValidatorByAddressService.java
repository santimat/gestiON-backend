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

    /**
     * Al actualizar, el comercio ya tiene esa direccion: el chequeo tiene que excluirlo, si no el
     * comercio no podria guardar sus propios datos sin cambiar la direccion.
     */
    public void checkExistingCommerceByAddressAndIdNot(String address, Long id) {
        if (commerceRepository.existsByAddressAndIdNot(address, id))
            throw new DuplicateResourceException(ErrorCode.COMMERCE_ADDRESS_ALREADY_EXISTS, "Commerce with address " + address + " already exists");
    }
}
