package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Construye el {@link ClientResponse} de un cliente con el resumen de cuenta corriente siempre
 * calculado (salesCount, pendingSalesCount, totalSpent, totalPending). Todos los endpoints que
 * devuelven un cliente pasan por aca, para que el shape de la respuesta sea el mismo en todos lados.
 */
@Service
@AllArgsConstructor
public class ClientResponseAssembler {

    private final ClientSaleStatsService clientSaleStatsService;

    public ClientResponse toResponse(Client client, Long commerceId) {
        JpaSaleRepository.ClientSalesSummary stats =
                clientSaleStatsService.getClientSaleStats(client.getId(), commerceId);

        Long salesCount = stats != null && stats.getPaidSalesCount() != null
                ? stats.getPaidSalesCount().longValue()
                : 0L;
        Long pendingSalesCount = stats != null && stats.getPendingSalesCount() != null
                ? stats.getPendingSalesCount().longValue()
                : 0L;
        BigDecimal totalSpent = stats != null && stats.getTotalSpent() != null
                ? stats.getTotalSpent()
                : BigDecimal.ZERO;
        BigDecimal totalPending = stats != null && stats.getTotalPending() != null
                ? stats.getTotalPending()
                : BigDecimal.ZERO;

        return ClientMapper.toResponse(client, salesCount, pendingSalesCount, totalSpent, totalPending);
    }
}
