package com.gestion.controller.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sellPoint.SellPointCashierAddRequest;
import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.service.sellPoint.SellPointCashierAdderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellpoints")
@AllArgsConstructor
public class SellPointCashierPostController {
    private final SellPointCashierAdderService sellPointCashierAdderService;

    @PostMapping("/{id}/cashiers")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SellPointResponse> addCashier(@PathVariable Long id,
                                                        @RequestBody @Valid SellPointCashierAddRequest request,
                                                        @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SellPointResponse response = sellPointCashierAdderService
                .addCashier(id, request.cashierId(), authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
