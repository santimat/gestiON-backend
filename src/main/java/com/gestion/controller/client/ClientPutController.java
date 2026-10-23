package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.service.client.ClientUpdaterService;
import com.gestion.service.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientPutController {
    private final ClientUpdaterService clientUpdaterService;
    private final JwtService jwtService;

    @PutMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @RequestBody @Valid ClientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ClientResponse updated = clientUpdaterService.updateClient(id, principal.getCommerceId(), request);
        return ResponseEntity.ok(updated);
    }
}
