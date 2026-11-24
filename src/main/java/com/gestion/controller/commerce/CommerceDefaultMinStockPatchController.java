package com.gestion.controller.commerce;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.commerce.CommerceDefaultMinStockRequest;
import com.gestion.dto.response.commerce.CommerceDefaultMinStockResponse;
import com.gestion.service.commerce.CommerceDefaultMinStockUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommerceDefaultMinStockPatchController {
    private final CommerceDefaultMinStockUpdaterService commerceDefaultMinStockUpdaterService;

    @PatchMapping("/default-min-stock")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CommerceDefaultMinStockResponse> updateDefaultMinStock(
            @Valid @RequestBody CommerceDefaultMinStockRequest request,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CommerceDefaultMinStockResponse response = commerceDefaultMinStockUpdaterService
                .updateDefaultMinStock(request.defaultMinStock(), authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
