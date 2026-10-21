package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientUpdaterService {
    private final JpaClientRepository jpaClientRepository;

    @Transactional
    public ClientResponse updateClient(ClientRequest request) {
        Client clientToUpdate = jpaClientRepository.findById();

        clientToUpdate.setName(request.name());
        clientToUpdate.setLastName(request.lastName());
        clientToUpdate.setAddress(request.address());
        clientToUpdate.setPhoneNumber(request.phoneNumber());
        clientToUpdate.setDni(request.dni());
        jpaClientRepository.save(clientToUpdate);
        return ClientMapper.toResponse(clientToUpdate);
    }
}
