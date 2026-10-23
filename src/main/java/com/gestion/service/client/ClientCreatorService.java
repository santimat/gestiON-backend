package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class ClientCreatorService {
    private final JpaClientRepository clientRepository;
    private final ClientValidatorByDniService validator;

    @Transactional
    public ClientResponse createClient(Long commerceId, ClientRequest request) {
        validator.checkExistingClientByDni(request, commerceId);

        Client newClient = ClientMapper.toEntity(request);
        newClient.setCommerceId(commerceId);
        newClient.setCreatedAt(LocalDate.now());
        Client saved = clientRepository.save(newClient);
        return ClientMapper.toResponse(saved);
    }
}
