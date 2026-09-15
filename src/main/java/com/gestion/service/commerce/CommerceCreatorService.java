package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.response.commerce.CommerceResponse;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.service.file.FileUploaderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceCreatorService {
    private final JpaCommerceRepository commerceRepository;
    private final FileUploaderService fileUploaderService;

    public CommerceResponse createCommerce(CommerceRequest request) {
        if (commerceRepository.existsByCuit(request.cuit())) {
            throw new DuplicateResourceException("Commerce with cuit " + request.cuit() + " already exists");
        }

        // TODO logica de guardado de la imagen en el storage y setear la url en commerce.setLogoUrl(url);
        String commerceLogoName = fileUploaderService.uploadFile(request.logo());

        Commerce commerce = CommerceMapper.toEntity(request);
        commerce.setLogoName(commerceLogoName);
        return CommerceMapper.toResponse(commerceRepository.save(commerce));
    }
}
