package com.gestion.service.sale;

import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SaleGetAllService {
    private final JpaSaleRepository jpaSaleRepository;

    public Page<Sale> findAll(Pageable pageable) {
        return jpaSaleRepository.findAll(pageable);
    }
}
