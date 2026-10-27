package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.service.client.ClientUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientPutController {
    private final ClientUpdaterService clientUpdaterService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @RequestBody @Valid ClientRequest request,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(ClientMapper.toResponse(
                clientUpdaterService.updateClient(id, request, authenticatedUser)));
    }
}
