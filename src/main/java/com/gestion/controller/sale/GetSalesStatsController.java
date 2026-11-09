package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SalesStatsResponse;
import com.gestion.service.sale.SalesStatsService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class GetSalesStatsController {

    private final SalesStatsService salesStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SalesStatsResponse> getSalesStats(
            @RequestParam @NotNull LocalDate startDate,
            @RequestParam @NotNull LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(salesStatsService.getSalesStats(
                authenticatedUser.getCommerceId(), startDate, endDate));
    }
}