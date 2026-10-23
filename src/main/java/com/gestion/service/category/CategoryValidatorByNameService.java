package com.gestion.service.category;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryValidatorByNameService {
    private final JpaCategoryRepository categoryRepository;

    public void checkExistingCategoryByName(Long commerceId, CategoryRequest request) {
        if (categoryRepository.existsByCommerceIdAndNameIgnoreCase(commerceId, request.name().strip())) {
            throw new DuplicateResourceException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                    "Category with name '" + request.name() + "' already exists in this commerce");
        }
    }

    public void checkExistingCategoryByName(Long commerceId, Long categoryIdToExclude, CategoryRequest request) {
        if (categoryRepository.existsByCommerceIdAndNameIgnoreCaseAndIdNot(commerceId, request.name().strip(), categoryIdToExclude)) {
            throw new DuplicateResourceException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                    "Category with name '" + request.name() + "' already exists in this commerce");
        }
    }
}
