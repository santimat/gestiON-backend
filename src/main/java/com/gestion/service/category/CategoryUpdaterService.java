package com.gestion.service.category;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CategoryUpdaterService {
    private final CategoryFinderByIdAndCommerceIdService categoryFinderByIdAndCommerceIdService;
    private final CategoryValidatorByNameService categoryValidatorByNameService;

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, Long commerceId) {
        Category categoryToUpdate = categoryFinderByIdAndCommerceIdService
                .findCategoryByIdAndCommerceId(categoryId, commerceId);

        request = new CategoryRequest(request.name().strip(), request.description());
        categoryValidatorByNameService.checkExistingCategoryByName(commerceId, categoryId, request);

        categoryToUpdate.setName(request.name());
        categoryToUpdate.setDescription(request.description());
        return CategoryMapper.toResponse(categoryToUpdate);
    }
}
