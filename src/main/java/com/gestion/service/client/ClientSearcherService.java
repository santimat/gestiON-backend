package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
public class ClientSearcherService {
    private final JpaClientRepository jpaClientRepository;
    private final ClientResponseAssembler clientResponseAssembler;

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

        // una sola query de resumen para toda la pagina
        Map<Long, ClientResponse> responses = clientResponseAssembler
                .toResponses(clients.getContent(), commerceId);

        return clients.map(client -> responses.get(client.getId()));
    }
}
