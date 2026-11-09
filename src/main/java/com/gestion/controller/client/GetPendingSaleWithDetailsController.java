package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.PendingSalesWithDetailsResponse;
import com.gestion.service.client.FindPendingSaleWithDetailsByClientIdService;
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
public class GetPendingSaleWithDetailsController {

    private final FindPendingSaleWithDetailsByClientIdService findPendingSaleWithDetailsByClientIdService;

    @GetMapping("/{clientId}/pending-sales-with-details")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<PendingSalesWithDetailsResponse> getPendingSalesWithDetails(
            @PathVariable Long clientId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(
                findPendingSaleWithDetailsByClientIdService
                        .findPendingSalesWithDetailsByClientId(clientId, authenticatedUser.getCommerceId()));
    }
}