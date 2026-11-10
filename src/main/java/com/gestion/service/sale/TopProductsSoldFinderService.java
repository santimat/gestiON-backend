package com.gestion.service.sale;

import com.gestion.dto.response.sale.TopProductsSoldResponse;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TopProductsSoldFinderService {

    private final JpaSaleRepository jpaSaleRepository;

    public List<TopProductsSoldResponse> getTopProductsSold(Long commerceId, int limit) {
        return jpaSaleRepository.getTopProductsSold(commerceId, limit);
    }
}