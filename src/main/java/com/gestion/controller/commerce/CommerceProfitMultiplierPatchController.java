package com.gestion.controller.commerce;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.commerce.CommerceProfitMultiplierRequest;
import com.gestion.dto.response.commerce.CommerceProfitMultiplierResponse;
import com.gestion.service.commerce.CommerceProfitMultiplierUpdaterService;
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
public class CommerceProfitMultiplierPatchController {
    private final CommerceProfitMultiplierUpdaterService commerceProfitMultiplierUpdaterService;

    @PatchMapping("/profit-multiplier")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CommerceProfitMultiplierResponse> updateProfitMultiplier(
            @Valid @RequestBody CommerceProfitMultiplierRequest request,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CommerceProfitMultiplierResponse response = commerceProfitMultiplierUpdaterService
                .updateProfitMultiplier(request.profitMultiplier(), authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
