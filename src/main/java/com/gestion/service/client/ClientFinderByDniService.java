package com.gestion.service.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientFinderByDniService {
    private final JpaClientRepository clientRepository;

    public ClientResponse findByDni(String dni) {
        Client client = clientRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND, "Client with DNI " + dni + " not found"));
        return ClientMapper.toResponse(client);
    }
}
