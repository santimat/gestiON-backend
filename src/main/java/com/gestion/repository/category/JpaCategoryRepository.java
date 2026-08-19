package com.gestion.repository.category;

import com.gestion.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {
}
