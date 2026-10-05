package com.gestion.service.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCategoryRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryCreatorService {
    private final JpaCategoryRepository categoryRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;

    public CategoryResponse createCategory(CategoryRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user or commerce ID is null");
        }
        Commerce commerce = commerceFinderByIdService.findById(authenticatedUser.getCommerceId());
        Category newCategory = categoryRepository.save(CategoryMapper.toEntity(request, commerce));
        return CategoryMapper.toResponse(newCategory);
    }
}
