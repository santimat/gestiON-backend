package com.gestion.controller.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellpoint.SellPointResponse;
import com.gestion.service.sellpoint.SellPointSearcherService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sell-points")
@AllArgsConstructor
public class SellPointsGetController {
    private final SellPointSearcherService sellPointSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<SellPointResponse>> getAllSellPoints(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortDir);
        Sort sortConfig = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sortConfig);

        return ResponseEntity.ok(sellPointSearcherService.searchSellPoints(authenticatedUser, pageable));
    }
}
