package com.gestion.mappers;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        return category;
    }
}
