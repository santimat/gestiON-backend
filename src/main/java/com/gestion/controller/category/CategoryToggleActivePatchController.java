package com.gestion.controller.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryUpdateActiveResponse;
import com.gestion.service.category.CategoryActiveUpdaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryToggleActivePatchController {
    private final CategoryActiveUpdaterService categoryActiveUpdaterService;

    @PatchMapping("/{categoryId}/toggle-active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryUpdateActiveResponse> toggleCategoryActiveStatus(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CategoryUpdateActiveResponse response = categoryActiveUpdaterService
                .toggleActive(categoryId, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
