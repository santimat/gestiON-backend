package com.gestion.controller.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sellpoint.SellPointRequest;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.service.sellpoint.SellPointCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sell-points")
@AllArgsConstructor
public class SellPointPostController {
    private final SellPointCreatorService sellPointCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SellPointResponse> createSellPoint(@RequestBody @Valid SellPointRequest request,
                                                             @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SellPointResponse response = sellPointCreatorService.createSellPoint(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
