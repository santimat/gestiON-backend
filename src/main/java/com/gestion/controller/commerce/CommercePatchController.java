package com.gestion.controller.commerce;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.commerce.CommercePatchRequest;
import com.gestion.dto.response.commerce.CurrentCommerceResponse;
import com.gestion.service.commerce.CommercePartialUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommercePatchController {

    private final CommercePartialUpdaterService commercePartialUpdaterService;

    @PatchMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CurrentCommerceResponse> patchCommerce(
            @Valid @ModelAttribute CommercePatchRequest request,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CurrentCommerceResponse response =
                commercePartialUpdaterService.partialUpdate(request, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}