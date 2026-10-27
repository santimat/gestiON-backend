package com.gestion.controller.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sellpoint.SellPointUpdateRequest;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.service.sellpoint.SellPointUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sell-points")
@AllArgsConstructor
public class SellPointPutController {
    private final SellPointUpdaterService sellPointUpdaterService;

    @PutMapping("/{sellPointId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SellPointResponse> updateSellPoint(@PathVariable Long sellPointId,
                                                             @RequestBody @Valid SellPointUpdateRequest request,
                                                             @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SellPointResponse response = sellPointUpdaterService.updateSellPoint(sellPointId, request,
                authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
