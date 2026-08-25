package com.gestion.service.sale;

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
        Sale sale = new Sale();
        return jpaSaleRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Sale with id " + id + "not found"));

    }
}
