package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ClientSearcherService {
    private final JpaClientRepository jpaClientRepository;
    private final ClientResponseAssemblerService clientResponseAssembler;
    private final ClientSearchTextNormalizerService clientSearchTextNormalizer;

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(Long commerceId,
                                             String searchText,
                                             Pageable pageable) {
        String effectiveSearchText = clientSearchTextNormalizer.normalize(searchText);

        Page<Client> clients = jpaClientRepository
                .findAllWithFilters(commerceId, effectiveSearchText, pageable);

        // una sola query de resumen para toda la pagina
        Map<Long, ClientResponse> responses = clientResponseAssembler
                .toResponses(clients.getContent(), commerceId);

        return clients.map(client -> responses.get(client.getId()));
    }
}
