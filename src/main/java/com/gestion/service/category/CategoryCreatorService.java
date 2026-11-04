package com.gestion.service.category;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCategoryRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryCreatorService {
    private final JpaCategoryRepository categoryRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final CategoryValidatorByNameService categoryValidatorByNameService;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long commerceId) {
        if (commerceId == null) {
            throw new AccessDeniedException("Commerce ID is null");
        }

        CategoryRequest parsedRequest = new CategoryRequest(request.name().strip(), request.description());
        categoryValidatorByNameService.checkExistingCategoryByName(commerceId, parsedRequest);

        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        Category newCategory = categoryRepository.save(CategoryMapper.toEntity(parsedRequest, commerce));
        return CategoryMapper.toResponse(newCategory);
    }
}
