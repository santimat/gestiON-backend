package com.gestion.service.client;

import com.gestion.config.UserPrincipal;
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
    public Page<ClientResponse> searchClients(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<Client> clients = jpaClientRepository
                .findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);
        return clients.map(ClientMapper::toResponse);
    }
}
