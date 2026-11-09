package com.gestion.service.client;

import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientSaleStatsService {

    private final JpaSaleRepository jpaSaleRepository;

    public JpaSaleRepository.ClientSalesSummary getClientSaleStats(Long clientId, Long commerceId) {
        return jpaSaleRepository.countSalesAndSpentByClient(clientId, commerceId);
    }
}