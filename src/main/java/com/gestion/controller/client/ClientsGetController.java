package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.service.client.ClientSearcherService;
import com.gestion.utils.PageableFactory;
import com.gestion.utils.SortableProperties;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientsGetController {
    private final ClientSearcherService clientSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.CLIENT);

        return ResponseEntity.ok(clientSearcherService.searchClients(
                authenticatedUser.getCommerceId(), searchText, name, lastName, pageable));
    }
}