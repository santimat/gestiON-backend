package com.gestion.service.category;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import com.gestion.service.product.ProductCounterByCategoryIds;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CategorySearcherService {
    private final JpaCategoryRepository jpaCategoryRepository;
    private ProductCounterByCategoryIds productCounterByCategoryIds;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(Long commerceId, String categoryName, Pageable pageable) {
        Page<Category> categories = jpaCategoryRepository.findAllWithFilters(commerceId, categoryName, pageable);

        List<Long> categoryIds = categories.getContent().stream()
                .map(Category::getId)
                .filter(Objects::nonNull)
                .toList();


        Map<Long, Long> productCountsByCategoryId = productCounterByCategoryIds.countProductsByCategoryIds(commerceId
                , categoryIds);

        return categories.map(category -> CategoryMapper.toResponse(category,
                productCountsByCategoryId.getOrDefault(category.getId(), 0L)));
    }
}
