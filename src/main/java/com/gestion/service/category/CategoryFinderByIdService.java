package com.gestion.service.category;

import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryFinderByIdService {
    private final JpaCategoryRepository categoryRepository;

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + id + " not found"));
    }
}
