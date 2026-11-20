package com.gestion.service.sale;

import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.service.saledetail.SaleDetailCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class SalePersistenceService {
    private final JpaSaleRepository saleRepository;
    private final SaleDetailCreatorService detailCreator;

    @Transactional
    public SalePersistenceResult persist(Sale sale, List<SaleDetail> details) {
        Sale saved = saleRepository.save(sale);
        detailCreator.createDetails(saved, details);
        int itemCount = details.stream().mapToInt(SaleDetail::getQuantity).sum();
        return new SalePersistenceResult(saved, itemCount);
    }
}