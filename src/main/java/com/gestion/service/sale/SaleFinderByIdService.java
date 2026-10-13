package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SaleFinderByIdService {
    private final JpaSaleRepository jpaSaleRepository;

    public Sale findBy(Long id) {
        return jpaSaleRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SALE_NOT_FOUND, "Sale with id " + id + " not found"));
    }
}
