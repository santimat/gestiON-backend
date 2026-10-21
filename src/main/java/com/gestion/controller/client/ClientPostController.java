package com.gestion.controller.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.service.client.ClientCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientPostController {
    private final ClientCreatorService clientCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<ClientResponse> createClient(@RequestBody @Valid ClientRequest request) {
        ClientResponse newClient = clientCreatorService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newClient);
    }
}
