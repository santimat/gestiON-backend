package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.response.commerce.CommerceResponse;
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
    public CommerceResponse createCommerce(CommerceRequest request) {

        commerceValidatorByCuitService.checkExistingCommerceByCuit(request.cuit());
        commerceValidatorByAddressService.checkExistingCommerceByAddress(request.address());

        String commerceLogoName = fileUploaderService.uploadFile(request.logo());

        Commerce commerce = CommerceMapper.toEntity(request);
        commerce.setLogoName(commerceLogoName);
        Commerce newCommerce = commerceRepository.save(commerce);
        return CommerceMapper.toResponse(newCommerce);
    }
}
