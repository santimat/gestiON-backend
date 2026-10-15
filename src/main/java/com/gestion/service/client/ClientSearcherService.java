package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientSearcherService {
    private final JpaClientRepository jpaClientRepository;

    public Page<ClientResponse> searchClients(Long commerceId, Pageable pageable) {
        Page<Client> clients = jpaClientRepository.findByCommerceId(commerceId, pageable);
        return clients.map(ClientMapper::toResponse);
    }
}
