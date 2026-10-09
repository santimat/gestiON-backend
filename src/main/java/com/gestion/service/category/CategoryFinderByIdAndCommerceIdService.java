package com.gestion.service.category;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryFinderByIdAndCommerceIdService {
    private final JpaCategoryRepository categoryRepository;

    public Category findCategoryByIdAndCommerceId(Long id, Long commerceId) {
        return categoryRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, "Category with id " + id + " not found"));
    }
}
