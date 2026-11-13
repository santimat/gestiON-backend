package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.ProfitByPeriodResponse;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfitByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;

    public ProfitByPeriodResponse getProfitByPeriod(PeriodGranularity granularity, Long commerceId) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        return new ProfitByPeriodResponse(
                jpaSaleRepository.sumPayedTotalByCommerceBetween(
                        commerceId, range.start(), range.end()));
    }
}