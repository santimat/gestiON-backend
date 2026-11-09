package com.gestion.service.sale;

import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class AverageSaleValueByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public BigDecimal getAverageSaleValueByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        return jpaSaleRepository.getAverageSaleValueByPeriod(commerceId, startDate, endDate);
    }
}