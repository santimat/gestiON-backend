package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.TopProductsSoldResponse;
import com.gestion.service.sale.TopProductsSoldFinderService;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class TopProductsSoldGetController {

    private final TopProductsSoldFinderService topProductsSoldFinderService;

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TopProductsSoldResponse>> getTopProductsSold(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {
        return ResponseEntity.ok(topProductsSoldFinderService
                .getTopProductsSold(authenticatedUser.getCommerceId(), limit));
    }
}