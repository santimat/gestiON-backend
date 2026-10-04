package com.gestion.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Length(max = 100, message = "Category name's length must be lower than 3 characters")
        String name
) {
}
