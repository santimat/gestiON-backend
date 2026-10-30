package com.gestion.service.category;

import com.gestion.dto.project.CategoryProductCount;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CategoryProductCountMapperService {
    private final JpaProductRepository jpaProductRepository;

    public Map<Long, Long> mapProductCountsByCategoryIds(Long commerceId, List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        return jpaProductRepository.countByCategoryIds(commerceId, categoryIds).stream()
                .collect(Collectors.toMap(
                        CategoryProductCount::categoryId,
                        CategoryProductCount::productCount));
    }

}
