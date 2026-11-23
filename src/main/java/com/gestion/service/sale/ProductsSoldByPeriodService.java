package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.enums.PeriodGranularity;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductsSoldByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;
    private final HourlyProductGrouper hourlyGrouper;
    private final DailyProductGrouper dailyGrouper;
    private final WeeklyProductGrouper weeklyGrouper;

    public List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        List<SaleDetail> details = jpaSaleRepository.saleDetailsInPeriod(
                commerceId, range.start(), range.end());

        return switch (granularity) {
            case DAY -> hourlyGrouper.group(details);
            case WEEK -> dailyGrouper.group(details, range);
            case MONTH -> weeklyGrouper.group(details, range);
        };
    }
}