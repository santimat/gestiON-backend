package com.gestion.service.category;

import com.gestion.dto.request.category.CategoryRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCategoryRepository;
import com.gestion.service.commerce.CommerceAvailabilityValidatorService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryCreatorService {
    private final JpaCategoryRepository categoryRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final CategoryValidatorByNameService categoryValidatorByNameService;
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidatorService;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long commerceId) {
        commerceAvailabilityValidatorService.checkCommerceIsAvailable(commerceId);

        CategoryRequest parsedRequest = new CategoryRequest(request.name().strip(), request.description());
        categoryValidatorByNameService.checkExistingCategoryByName(commerceId, parsedRequest);

        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        Category newCategory = categoryRepository.save(CategoryMapper.toEntity(parsedRequest, commerce));
        return CategoryMapper.toResponse(newCategory);
    }
}
