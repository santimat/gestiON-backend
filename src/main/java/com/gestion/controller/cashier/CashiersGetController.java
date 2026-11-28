package com.gestion.controller.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.cashier.CashierSearcherService;
import com.gestion.utils.PageableFactory;
import com.gestion.utils.SortableProperties;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashiers")
@AllArgsConstructor
public class CashiersGetController {
    private final CashierSearcherService cashierSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<UserResponse>> getAllCashiers(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.USER);

        Page<UserResponse> cashiers = cashierSearcherService
                .searchCashiers(authenticatedUser.getCommerceId(), searchText, pageable);
        return ResponseEntity.ok(cashiers);
    }
}
