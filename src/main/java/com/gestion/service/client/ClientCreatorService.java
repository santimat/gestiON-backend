package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
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
    private final ClientValidatorByDniService clientValidatorByDniService;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public Client createClient(ClientRequest request, Long commerceId) {
        if (commerceId == null) {
            throw new AccessDeniedException(
                    "Authenticated user does not have a commerce associated");
        }

        clientValidatorByDniService.checkExistingClientByDni(request, commerceId);

        Client newClient = ClientMapper.toEntity(request,
                commerceFinderByIdService.findCommerceById(commerceId));
        return clientRepository.save(newClient);
    }
}
