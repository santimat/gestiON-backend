package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SalesStatsByPeriodResponse;
import com.gestion.enums.PeriodGranularity;
import com.gestion.service.sale.SalesStatsByProfitGetterService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SalesStatsByPeriodGetController {

    private final SalesStatsByProfitGetterService salesStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SalesStatsByPeriodResponse> getSalesStats(
            @RequestParam @NotNull PeriodGranularity granularity,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(salesStatsService.getSalesStats(
                authenticatedUser.getCommerceId(), granularity));
    }
}