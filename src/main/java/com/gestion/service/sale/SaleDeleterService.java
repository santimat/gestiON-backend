package com.gestion.service.sale;

import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SaleDeleterService {
    private final JpaSaleRepository jpaSaleRepository;
    private final SaleFinderByIdService saleFinderByIdService;

    public void delete(Long id) {
        Sale sale = saleFinderByIdService.findBy(id);
        jpaSaleRepository.delete(sale);
    }
}
