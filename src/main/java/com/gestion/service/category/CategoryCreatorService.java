package com.gestion.service.category;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryCreatorService {
    private final JpaCategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public Category createCategory(CategoryRequest request) {
        return categoryRepository.save(categoryMapper.toEntity(request));
    }
}
