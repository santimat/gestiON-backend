package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceStatsResponse;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class CommerceStatsService {
    private final JpaCommerceRepository commerceRepository;

    public CommerceStatsResponse getStats() {
        Long active = commerceRepository.countByActive(true);
        Long total = commerceRepository.count();
        Long inactive = total - active;

        return new CommerceStatsResponse(total, active, inactive);
    }
}
