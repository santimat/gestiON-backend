package com.gestion.controller.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.service.category.CategoryFinderByIdAndCommerceIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryGetController {
    private final CategoryFinderByIdAndCommerceIdService categoryFinderByIdAndCommerceIdService;

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId,
                                                            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        Category category = categoryFinderByIdAndCommerceIdService
                .findCategoryByIdAndCommerceId(categoryId, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(CategoryMapper.toResponse(category));
    }
}
