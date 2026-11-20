package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.enums.PeriodGranularity;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProductsSoldByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;
    private final Map<PeriodGranularity, PeriodGroupingStrategy> strategies;

    public List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        List<SaleDetail> details = jpaSaleRepository.saleDetailsInPeriod(
                commerceId, range.start(), range.end());

        PeriodGroupingStrategy strategy = strategies.get(granularity);
        return strategy.group(details, range);
    }
}