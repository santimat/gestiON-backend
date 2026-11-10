package com.gestion.service.sale;

import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.dto.response.sale.SalesStatsByPeriodResponse;
import com.gestion.enums.PeriodGranularity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class SalesStatsByProfitGetterService {

    private final SalesCountByPeriodService salesCountByPeriodService;
    private final AverageSaleValueByPeriodService averageSaleValueByPeriodService;
    private final ProductsSoldByPeriodService productsSoldByPeriodService;
    private final ProfitByPeriodService profitByPeriodService;

    public SalesStatsByPeriodResponse getSalesStats(Long commerceId, PeriodGranularity granularity) {
        long salesCount = salesCountByPeriodService.getSalesCountByPeriod(commerceId, granularity);
        BigDecimal averageSaleValue = averageSaleValueByPeriodService.getAverageSaleValueByPeriod(commerceId, granularity);
        List<PeriodProductCount> productsSoldByPeriod = productsSoldByPeriodService
                .getProductsSoldByPeriod(commerceId, granularity);
        BigDecimal profit = profitByPeriodService
                .getProfitByPeriod(granularity, commerceId)
                .profit();

        return new SalesStatsByPeriodResponse(salesCount, averageSaleValue, productsSoldByPeriod, profit);
    }
}