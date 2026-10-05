package com.gestion.service.category;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategorySearcherService {
    private final JpaCategoryRepository jpaCategoryRepository;

    public Page<CategoryResponse> searchCategories(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<Category> categories = jpaCategoryRepository.findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);
        return categories.map(CategoryMapper::toResponse);
    }
}
