package com.gestion.service.sale;

import com.gestion.dto.response.sale.ProfitByPeriodResponse;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfitByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public ProfitByPeriodResponse getProfitByPeriod(PeriodGranularity granularity, Long commerceId) {
        return new ProfitByPeriodResponse(
                jpaSaleRepository.getProfitByPeriod(
                        commerceId, granularity));
    }
}