package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientCreatorService {
    private final JpaClientRepository clientRepository;

    @Transactional
    public Client createClient(ClientRequest request) {
        if (clientRepository.existsByDni(request.dni())) {
            throw new DuplicateResourceException(ErrorCode.CLIENT_DNI_ALREADY_EXISTS, "Client with DNI " + request.dni() + " already exists");
        }

        Client newClient = ClientMapper.toEntity(request);
        return clientRepository.save(newClient);
    }
}
