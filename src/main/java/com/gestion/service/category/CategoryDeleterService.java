package com.gestion.service.category;

import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryDeleterService {
    private final JpaCategoryRepository jpaCategoryRepository;
    private final CategoryFinderByIdService categoryFinderByIdService;

    public void delete(Long id){
        Category category = categoryFinderByIdService.findById(id);
        jpaCategoryRepository.delete(category);
    }
}
