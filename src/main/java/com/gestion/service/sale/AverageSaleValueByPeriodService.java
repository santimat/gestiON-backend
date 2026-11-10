package com.gestion.service.sale;

import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class AverageSaleValueByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public BigDecimal getAverageSaleValueByPeriod(Long commerceId, PeriodGranularity granularity) {
        return jpaSaleRepository.getAverageSaleValueByPeriod(commerceId, granularity);
    }
}