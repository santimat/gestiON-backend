package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ClientSearcherService {
    private final JpaClientRepository jpaClientRepository;
    private final ClientSaleStatsService clientSaleStatsService;

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(Long commerceId,
                                             String searchText,
                                             String name,
                                             String lastName,
                                             Pageable pageable) {
        String effectiveSearchText = searchText;
        if ((effectiveSearchText == null || effectiveSearchText.isBlank())
                && (name != null || lastName != null)) {
            StringBuilder builder = new StringBuilder();
            if (name != null) {
                builder.append(name).append(" ");
            }
            if (lastName != null) {
                builder.append(lastName);
            }
            effectiveSearchText = builder.toString().trim();
            if (effectiveSearchText.isBlank()) {
                effectiveSearchText = null;
            }
        } else if (effectiveSearchText != null && effectiveSearchText.isBlank()) {
            effectiveSearchText = null;
        }

        Page<Client> clients = jpaClientRepository
                .findAllWithFilters(commerceId, effectiveSearchText, pageable);

        return clients.map(client -> {
            var stats = clientSaleStatsService.getClientSaleStats(client.getId(), commerceId);
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
        });
    }
}