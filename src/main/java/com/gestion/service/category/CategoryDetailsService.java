package com.gestion.service.category;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.model.Category;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CategoryDetailsService {
    private final CategoryFinderByIdAndCommerceIdService categoryFinderByIdAndCommerceIdService;
    private final CategoryDetailsResponseBuilderService categoryDetailsResponseBuilderService;

    @Transactional(readOnly = true)
    public CategoryResponse findCategoryByIdAndCommerceId(Long categoryId, Long commerceId) {
        Category category = categoryFinderByIdAndCommerceIdService
                .findCategoryByIdAndCommerceId(categoryId, commerceId);

        return categoryDetailsResponseBuilderService.buildResponse(category, commerceId);
    }
}
