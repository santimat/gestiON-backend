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

        CategoryRequest parsedRequest = new CategoryRequest(request.name().strip(), request.description());
        categoryValidatorByNameService.checkExistingCategoryByName(commerceId, categoryId, parsedRequest);

        categoryToUpdate.setName(parsedRequest.name());
        categoryToUpdate.setDescription(parsedRequest.description());
        return CategoryMapper.toResponse(categoryToUpdate);
    }
}
