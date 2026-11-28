package com.gestion.service.client;

import com.gestion.dto.project.client.ClientAccountSummary;
import com.gestion.dto.project.client.ClientPendingPaymentSum;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ClientAccountSummaryService {

    private final JpaSaleRepository jpaSaleRepository;

    @Transactional(readOnly = true)
    public Map<Long, ClientAccountSummary> findByClientIds(Collection<Long> clientIds, Long commerceId) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Map.of();
        }

        List<ClientAccountSummary> summaries = jpaSaleRepository
                .findAccountSummariesByClientIds(clientIds, commerceId);

        // la deuda real es el total de las ventas pendientes menos lo que ya se pago de ellas
        Map<Long, BigDecimal> paidByClientId = jpaSaleRepository
                .sumPendingSalesPaymentsByClientIds(clientIds, commerceId).stream()
                .collect(Collectors.toMap(ClientPendingPaymentSum::clientId, ClientPendingPaymentSum::totalPaid));

        Map<Long, ClientAccountSummary> byClientId = new HashMap<>();
        for (ClientAccountSummary summary : summaries) {
            BigDecimal paid = paidByClientId.getOrDefault(summary.clientId(), BigDecimal.ZERO);
            byClientId.put(summary.clientId(), normalize(summary, paid));
        }
        for (Long clientId : clientIds) {
            byClientId.computeIfAbsent(clientId, this::emptySummary);
        }
        return byClientId;
    }

    // la query puede devolver null en los sum cuando el cliente no tiene ventas de ese estado
    private ClientAccountSummary normalize(ClientAccountSummary summary, BigDecimal paidOfPending) {
        BigDecimal pendingTotal = summary.totalPending() == null ? BigDecimal.ZERO : summary.totalPending();
        return new ClientAccountSummary(
                summary.clientId(),
                summary.salesCount() == null ? 0L : summary.salesCount(),
                summary.pendingSalesCount() == null ? 0L : summary.pendingSalesCount(),
                summary.totalSpent() == null ? BigDecimal.ZERO : summary.totalSpent(),
                pendingTotal.subtract(paidOfPending));
    }

    private ClientAccountSummary emptySummary(Long clientId) {
        return new ClientAccountSummary(clientId, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
