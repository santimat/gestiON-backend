package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientValidatorByDniService {
    private final JpaClientRepository clientRepository;

    public void checkExistingClientByDni(ClientRequest request, Long commerceId) {
        if (clientRepository.existsByDniAndCommerceId(request.dni().strip(), commerceId)) {
            throw new DuplicateResourceException(ErrorCode.CLIENT_DNI_ALREADY_EXISTS,
                    "Client with DNI " + request.dni() + " already exists in this commerce");
        }
    }

    // para el update: ignora al propio cliente
    public void checkExistingClientByDni(ClientRequest request, Long commerceId, Long clientIdToExclude) {
        if (clientRepository.existsByDniAndCommerceIdAndIdNot(request.dni().strip(), commerceId, clientIdToExclude)) {
            throw new DuplicateResourceException(ErrorCode.CLIENT_DNI_ALREADY_EXISTS,
                    "Client with DNI " + request.dni() + " already exists in this commerce");
        }
    }
}
