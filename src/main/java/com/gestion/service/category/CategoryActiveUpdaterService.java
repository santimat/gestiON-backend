package com.gestion.service.category;

import com.gestion.dto.response.category.CategoryUpdateActiveResponse;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryActiveUpdaterService {
    private final CategoryFinderByIdAndCommerceIdService categoryFinderByIdAndCommerceIdService;
    private final JpaCategoryRepository categoryRepository;

    @Transactional
    public CategoryUpdateActiveResponse toggleActive(Long categoryId, Long commerceId) {
        Category categoryToUpdate = categoryFinderByIdAndCommerceIdService
                .findCategoryByIdAndCommerceId(categoryId, commerceId);
        categoryToUpdate.setActive(!categoryToUpdate.isActive());
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        return new CategoryUpdateActiveResponse(
                updatedCategory.getId(),
                updatedCategory.isActive()
        );
    }
}
