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
        category.setCommerce(commerce);
        return category;
    }

    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
