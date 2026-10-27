package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.mappers.ClientMapper;
import com.gestion.service.client.ClientFinderByDniService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientGetController {
    private final ClientFinderByDniService clientFinderByDniService;

    @GetMapping("/{dni}")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<ClientResponse> getClientByDni(
            @PathVariable String dni,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(ClientMapper.toResponse(
                clientFinderByDniService.findByDni(dni, authenticatedUser.getCommerceId())));
    }
}
