package com.gestion.controller.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.model.Client;
import com.gestion.service.client.ClientUpdaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
@AllArgsConstructor
public class ClientPutController {
    private final ClientUpdaterService clientUpdaterService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ClientResponse> updateClient(ClientRequest request) {
        Client clientUpdated = clientUpdaterService.updateClient(request);
        return ResponseEntity.ok(ClientMapper.toResponse(clientUpdated));
    }
}
