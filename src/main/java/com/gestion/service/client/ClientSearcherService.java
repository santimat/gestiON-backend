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

@Service
@AllArgsConstructor
public class ClientSearcherService {
    private final JpaClientRepository jpaClientRepository;

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(Long commerceId, String searchText, String dni, Pageable pageable) {
        
        if (searchText != null && searchText.isBlank()) {
            searchText = null;
        }

        if (dni != null && dni.isBlank()) {
            dni = null;
        }

        Page<Client> clients = jpaClientRepository
                .findAllWithFilters(commerceId, searchText, dni, pageable);
        return clients.map(ClientMapper::toResponse);
    }
}