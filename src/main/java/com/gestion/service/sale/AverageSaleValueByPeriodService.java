package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@AllArgsConstructor
public class AverageSaleValueByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;

    public BigDecimal getAverageSaleValueByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        Double avg = jpaSaleRepository.averageSaleValueByPeriod(commerceId, range.start(), range.end());
        return avg == null ? BigDecimal.ZERO : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }
}