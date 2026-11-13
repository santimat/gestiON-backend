package com.gestion.service.category;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryDetailsResponseBuilderService {
    private final CategoryProductCounterService categoryProductCounterService;

    @Transactional(readOnly = true)
    public CategoryResponse buildResponse(Category category, Long commerceId) {
        long productCount = categoryProductCounterService
                .countProductsByCategoryIds(commerceId, List.of(category.getId()))
                .getOrDefault(category.getId(), 0L);

        return CategoryMapper.toResponse(category, productCount);
    }
}
