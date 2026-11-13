package com.gestion.service.client;

import com.gestion.dto.project.ClientAccountSummary;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@AllArgsConstructor
public class ClientAccountSummaryService {

    private final JpaSaleRepository jpaSaleRepository;

    public Map<Long, ClientAccountSummary> findByClientIds(Collection<Long> clientIds, Long commerceId) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Map.of();
        }

        List<ClientAccountSummary> summaries = jpaSaleRepository
                .findAccountSummariesByClientIds(clientIds, commerceId);

        Map<Long, ClientAccountSummary> byClientId = new HashMap<>();
        for (ClientAccountSummary summary : summaries) {
            byClientId.put(summary.clientId(), normalize(summary));
        }
        for (Long clientId : clientIds) {
            byClientId.computeIfAbsent(clientId, this::emptySummary);
        }
        return byClientId;
    }

    // la query puede devolver null en los sum cuando el cliente no tiene ventas de ese estado
    private ClientAccountSummary normalize(ClientAccountSummary summary) {
        return new ClientAccountSummary(
                summary.clientId(),
                summary.salesCount() == null ? 0L : summary.salesCount(),
                summary.pendingSalesCount() == null ? 0L : summary.pendingSalesCount(),
                summary.totalSpent() == null ? BigDecimal.ZERO : summary.totalSpent(),
                summary.totalPending() == null ? BigDecimal.ZERO : summary.totalPending());
    }

    private ClientAccountSummary emptySummary(Long clientId) {
        return new ClientAccountSummary(clientId, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
