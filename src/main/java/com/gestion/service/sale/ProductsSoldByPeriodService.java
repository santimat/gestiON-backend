package com.gestion.service.sale;

import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductsSoldByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, PeriodGranularity granularity) {
        return jpaSaleRepository.getProductsSoldByPeriod(commerceId, granularity);
    }
}