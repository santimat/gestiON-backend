package com.gestion.service.sale;

import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class SalesCountByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public long getSalesCountByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        return jpaSaleRepository.getSalesCountByPeriod(commerceId, startDate, endDate);
    }
}