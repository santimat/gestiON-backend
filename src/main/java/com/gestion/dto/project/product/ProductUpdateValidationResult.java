package com.gestion.dto.project.product;

import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;

public record ProductUpdateValidationResult(
        Product product,
        Commerce commerce,
        Category category
) {
}