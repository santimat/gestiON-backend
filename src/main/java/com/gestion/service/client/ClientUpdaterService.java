package com.gestion.service.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.client.ClientRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientUpdaterService {
    private final JpaClientRepository jpaClientRepository;
    private final ClientValidatorByDniService clientValidatorByDniService;

    @Transactional
    public Client updateClient(Long clientId, ClientRequest request, UserPrincipal authenticatedUser) {
        Client clientToUpdate = jpaClientRepository
                .findByIdAndCommerceId(clientId, authenticatedUser.getCommerceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLIENT_NOT_FOUND,
                        "Client with id " + clientId + " not found in this commerce"));

        clientValidatorByDniService.checkExistingClientByDni(request, authenticatedUser.getCommerceId(), clientId);

        clientToUpdate.setName(request.name());
        clientToUpdate.setLastName(request.lastName());
        clientToUpdate.setAddress(request.address());
        clientToUpdate.setPhoneNumber(request.phoneNumber());
        clientToUpdate.setDni(request.dni());

        return clientToUpdate;
    }
}
