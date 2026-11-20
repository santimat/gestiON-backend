package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.model.SaleDetail;

import java.util.List;

public interface PeriodGroupingStrategy {
    List<PeriodProductCount> group(List<SaleDetail> details, PeriodRange range);
}