package com.gestion.controller.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.service.product.ProductSearcherService;
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
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductsGetController {
    private final ProductSearcherService productSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.PRODUCT);
        return ResponseEntity.ok(productSearcherService
                .searchProducts(authenticatedUser.getCommerceId(), categoryId, searchText, active, pageable));
    }
}
