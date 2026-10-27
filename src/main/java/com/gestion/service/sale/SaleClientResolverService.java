package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resuelve el cliente de una venta. Un clientId null significa venta anónima
 * (consumidor final), así que devuelve null sin error en ese caso.
 */
@Service
@AllArgsConstructor
public class SaleClientResolverService {
    private final JpaClientRepository jpaClientRepository;

    public Client resolveClient(Long clientId, Long commerceId) {
        if (clientId == null) {
            return null;
        }
        return jpaClientRepository.findByIdAndCommerceId(clientId, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CLIENT_NOT_FOUND,
                        "Client with id " + clientId + " not found in this commerce"));
    }
}
