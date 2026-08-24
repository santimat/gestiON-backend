package com.gestion.service.category;

import com.gestion.model.Category;
import com.gestion.repository.JpaCategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryGetAllService {
    private final JpaCategoryRepository jpaCategoryRepository;

    public Page<Category> findAll(Pageable pageable){
        return jpaCategoryRepository.findAll(pageable);
    }
}
