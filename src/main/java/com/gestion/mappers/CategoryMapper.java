package com.gestion.mappers;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.model.Category;

public class CategoryMapper {
    public static Category toEntity(CategoryRequest request) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.name());
        return category;
    }
}
