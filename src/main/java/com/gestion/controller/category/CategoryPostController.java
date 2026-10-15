package com.gestion.controller.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.service.category.CategoryCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryPostController {
    private final CategoryCreatorService categoryCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request,
                                                           @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CategoryResponse response = categoryCreatorService.createCategory(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
