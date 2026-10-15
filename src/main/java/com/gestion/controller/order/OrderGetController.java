package com.gestion.controller.order;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.service.order.OrderSearcherService;
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
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderGetController {
    private final OrderSearcherService orderSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        Sort sortConfig = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sortConfig);


        return ResponseEntity.ok(orderSearcherService.searchOrders(authenticatedUser, pageable));
    }


}
