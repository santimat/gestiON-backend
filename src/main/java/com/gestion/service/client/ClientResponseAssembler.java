package com.gestion.service.client;

import com.gestion.dto.project.ClientAccountSummary;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye el {@link ClientResponse} de un cliente con el resumen de cuenta corriente siempre
 * calculado (salesCount, pendingSalesCount, totalSpent, totalPending). Todos los endpoints que
 * devuelven un cliente pasan por aca, para que el shape de la respuesta sea el mismo en todos lados.
 */
@Service
@AllArgsConstructor
public class ClientResponseAssembler {

    private final ClientAccountSummaryService clientAccountSummaryService;

    /** Un cliente: una sola query de resumen. */
    public ClientResponse toResponse(Client client, Long commerceId) {
        Map<Long, ClientAccountSummary> summaries = clientAccountSummaryService
                .findByClientIds(List.of(client.getId()), commerceId);
        return toResponse(client, summaries);
    }

    /** Una pagina entera de clientes: una sola query de resumen para todos. */
    public Map<Long, ClientResponse> toResponses(List<Client> clients, Long commerceId) {
        List<Long> clientIds = clients.stream().map(Client::getId).toList();
        Map<Long, ClientAccountSummary> summaries = clientAccountSummaryService
                .findByClientIds(clientIds, commerceId);

        Map<Long, ClientResponse> responses = new LinkedHashMap<>();
        for (Client client : clients) {
            responses.put(client.getId(), toResponse(client, summaries));
        }
        return responses;
    }

    private ClientResponse toResponse(Client client, Map<Long, ClientAccountSummary> summaries) {
        ClientAccountSummary summary = summaries.get(client.getId());
        if (summary == null) {
            summary = new ClientAccountSummary(client.getId(), 0L, 0L, BigDecimal.ZERO,
                    BigDecimal.ZERO);
        }
        return ClientMapper.toResponse(client,
                summary.salesCount(),
                summary.pendingSalesCount(),
                summary.totalSpent(),
                summary.totalPending());
    }
}
