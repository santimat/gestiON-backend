package com.gestion.service.sale;

import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleDetailRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class SaleDetailCreatorService {
    private final JpaSaleDetailRepository jpaSaleDetailRepository;

    public void createDetails(Sale savedSale, List<SaleDetail> details) {
        for (SaleDetail detail : details) {
            detail.setSale(savedSale);
            jpaSaleDetailRepository.save(detail);
        }
    }
}
