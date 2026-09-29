package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.service.file.FileUploaderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceCreatorService {
    private final JpaCommerceRepository commerceRepository;
    private final FileUploaderService fileUploaderService;
    private final CommerceValidatorByCuitService commerceValidatorByCuitService;
    private final CommerceValidatorByAddressService commerceValidatorByAddressService;

    @Transactional
    public Commerce createCommerce(CommerceRequest request) {

        commerceValidatorByCuitService.checkExistingCommerceByCuit(request.cuit());
        commerceValidatorByAddressService.checkExistingCommerceByAddress(request.address());

        Commerce commerce = CommerceMapper.toEntity(request);

        commerce.setActive(true);
        return commerceRepository.save(commerce);
    }
}
