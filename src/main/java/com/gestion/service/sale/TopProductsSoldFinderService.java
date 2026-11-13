package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.TopProductsSoldResponse;
import com.gestion.enums.PeriodGranularity;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TopProductsSoldFinderService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;

    public List<TopProductsSoldResponse> getTopProductsSold(Long commerceId, int limit) {
        PeriodRange range = periodRangeService.resolveRange(PeriodGranularity.MONTH);
        return jpaSaleRepository.topProductsSoldByCommerceId(commerceId, range.start(), range.end(), PageRequest.of(0, limit));
    }
}