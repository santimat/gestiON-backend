package com.gestion.service.sale;

import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductsSoldByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;

    public List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        return jpaSaleRepository.getProductsSoldByPeriod(commerceId, startDate, endDate);
    }
}