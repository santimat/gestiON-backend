package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientUpdaterService {
    private final JpaClientRepository jpaClientRepository;
    private final ClientValidatorByDniService clientValidatorByDniService;

    @Transactional
    public ClientResponse updateClient(Long clientId, Long commerceId, ClientRequest request) {
        Client clientToUpdate = jpaClientRepository
                .findByIdAndCommerceId(clientId, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLIENT_NOT_FOUND,
                        "Client with id " + clientId + " not found"));

        clientValidatorByDniService.checkExistingClientByDni(request, commerceId, clientId);

        clientToUpdate.setName(request.name());
        clientToUpdate.setLastName(request.lastName());
        clientToUpdate.setAddress(request.address());
        clientToUpdate.setPhoneNumber(request.phoneNumber());
        clientToUpdate.setDni(request.dni());

        return ClientMapper.toResponse(clientToUpdate);
    }
}
