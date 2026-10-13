package com.gestion.controller.commerce;

import com.gestion.dto.request.commerce.CommerceWithOwnerUpdateRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.service.commerce.CommerceWithOwnerUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommerceWithOwnerPutController {
    private final CommerceWithOwnerUpdaterService commerceWithOwnerUpdaterService;

    @PutMapping("/{commerceId}/user/{userId}")
    @PreAuthorize("hasRole('SUDO')")
    public ResponseEntity<CommerceWithOwnerResponse> updateCommerceWithOwner(@Valid @ModelAttribute CommerceWithOwnerUpdateRequest request,
                                                                             @PathVariable Long commerceId, @PathVariable Long userId) {
        CommerceWithOwnerResponse response = commerceWithOwnerUpdaterService.updateCommerceWithOwner(request, commerceId, userId);
        return ResponseEntity.ok(response);
    }
}
