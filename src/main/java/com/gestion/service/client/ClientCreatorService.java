package com.gestion.service.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.client.ClientRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientCreatorService {
    private final JpaClientRepository clientRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public Client createClient(ClientRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user does not have a commerce associated");
        }

        if (clientRepository.existsByDniAndCommerceId(request.dni(), authenticatedUser.getCommerceId())) {
            throw new DuplicateResourceException(ErrorCode.CLIENT_DNI_ALREADY_EXISTS,
                    "Client with DNI " + request.dni() + " already exists in this commerce");
        }

        Client newClient = ClientMapper.toEntity(request,
                commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId()));
        return clientRepository.save(newClient);
    }
}
