package com.gestion.controller.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.sellpoint.SellPointCashiersFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sell-points")
@AllArgsConstructor
public class SellPointCashiersGetController {
    private final SellPointCashiersFinderService sellPointCashiersFinderService;

    @GetMapping("/{sellPointId}/cashiers")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<UserResponse>> getCashiersBySellPoint(
            @PathVariable Long sellPointId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortDir);
        Sort sortConfig = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sortConfig);

        return ResponseEntity.ok(sellPointCashiersFinderService.findCashiersBySellPoint(sellPointId, authenticatedUser, pageable));
    }
}
