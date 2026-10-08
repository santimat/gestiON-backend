package com.gestion.controller.order;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.order.OrderRequest;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.service.order.OrderCreatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderPostController {
    private final OrderCreatorService orderCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request,
                                                     @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        OrderResponse response = orderCreatorService.createOrder(request, authenticatedUser.getCommerceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
