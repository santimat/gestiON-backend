package com.gestion.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 3, max = 100, message = "Category name's length must be between 3 and 100 characters")
        String name
) {
}
