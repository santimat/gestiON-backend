package com.gestion.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Length(max = 50, message = "Category name's length must be 50 characters or fewer")
        String name,

        @Length(max = 200, message = "Category description's length must be 200 characters or fewer")
        String description
) {
}
