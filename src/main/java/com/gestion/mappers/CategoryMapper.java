package com.gestion.mappers;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.model.Category;
import com.gestion.model.Commerce;

public class CategoryMapper {
    public static Category toEntity(CategoryRequest request, Commerce commerce) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setCommerce(commerce);
        category.setActive(true);
        return category;
    }

    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getCommerce() != null ? category.getCommerce().getId() : null,
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
