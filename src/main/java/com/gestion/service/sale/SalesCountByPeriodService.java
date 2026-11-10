package com.gestion.service.sale;

import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SalesCountByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public long getSalesCountByPeriod(Long commerceId, PeriodGranularity granularity) {
        return jpaSaleRepository.getSalesCountByPeriod(commerceId, granularity);
    }
}