package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import com.gestion.service.commerce.CommerceAvailabilityValidatorService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientCreatorService {
    private final JpaClientRepository clientRepository;
    private final ClientValidatorByDniService clientValidatorByDniService;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidatorService;

    @Transactional
    public Client createClient(ClientRequest request, Long commerceId) {
        commerceAvailabilityValidatorService.checkCommerceIsAvailable(commerceId);

        clientValidatorByDniService.checkExistingClientByDni(request, commerceId);

        Client newClient = ClientMapper.toEntity(request,
                commerceFinderByIdService.findCommerceById(commerceId));
        return clientRepository.save(newClient);
    }
}
