package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SalesCountByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;

    public long getSalesCountByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        return jpaSaleRepository.countSalesByPeriod(commerceId, range.start(), range.end());
    }
}