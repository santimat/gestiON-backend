package com.gestion.service.sale;

import com.gestion.dto.request.sale.ProfitByPeriodRequest;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.dto.response.sale.SalesStatsResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class SalesStatsService {

    private final SalesCountByPeriodService salesCountByPeriodService;
    private final AverageSaleValueByPeriodService averageSaleValueByPeriodService;
    private final ProductsSoldByPeriodService productsSoldByPeriodService;
    private final ProfitByPeriodService profitByPeriodService;

    public SalesStatsResponse getSalesStats(Long commerceId, LocalDate startDate, LocalDate endDate) {
        long salesCount = salesCountByPeriodService.getSalesCountByPeriod(commerceId, startDate, endDate);
        BigDecimal averageSaleValue = averageSaleValueByPeriodService.getAverageSaleValueByPeriod(commerceId, startDate, endDate);
        List<PeriodProductCount> productsSoldByPeriod = productsSoldByPeriodService
                .getProductsSoldByPeriod(commerceId, startDate, endDate);
        BigDecimal profit = profitByPeriodService
                .getProfitByPeriod(new ProfitByPeriodRequest(startDate, endDate), commerceId)
                .profit();

        return new SalesStatsResponse(salesCount, averageSaleValue, productsSoldByPeriod, profit);
    }
}