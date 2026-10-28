package com.gestion.service.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.project.CategoryProductCount;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategorySearcherService {
    private final JpaCategoryRepository jpaCategoryRepository;
    private final JpaProductRepository jpaProductRepository;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<Category> categories = jpaCategoryRepository.findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);

        List<Long> categoryIds = categories.getContent().stream()
                .map(Category::getId)
                .filter(Objects::nonNull)
                .toList();
        if (categoryIds.isEmpty()) {
            return categories.map(category -> CategoryMapper.toResponse(category, 0));
        }

        Map<Long, Long> productCountsByCategoryId = jpaProductRepository
                .countByCategoryIds(authenticatedUser.getCommerceId(), categoryIds).stream()
                .collect(Collectors.toMap(
                        CategoryProductCount::getCategoryId,
                        CategoryProductCount::getProductCount));

        return categories.map(category -> CategoryMapper.toResponse(category,
                productCountsByCategoryId.getOrDefault(category.getId(), 0L)));
    }
}
