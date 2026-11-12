package com.gestion.controller.category;


import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.service.category.CategorySearcherService;
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
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoriesGetController {
    private final CategorySearcherService categorySearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String categoryName) {
        Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.CATEGORY);

        return ResponseEntity.ok(categorySearcherService
                .searchCategories(authenticatedUser.getCommerceId(), categoryName, pageable));
    }
}
