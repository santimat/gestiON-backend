package com.gestion.controller.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.service.sellPoint.SellPointSearcherService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellpoints")
@AllArgsConstructor
public class SellPointsGetController {
    private final SellPointSearcherService sellPointSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<SellPointResponse>> getAllSellPoints(Pageable pageable,
                                                                    @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(sellPointSearcherService.searchSellPoints(authenticatedUser, pageable));
    }
}
