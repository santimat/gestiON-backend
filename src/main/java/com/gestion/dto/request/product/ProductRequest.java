package com.gestion.dto.request.product;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product's name is required")
        @Length(max = 100, message = "Product's length must be lower than 100 characters")
        String name,
        @Length(max = 255, message = "Product's length must be lower than 255 characters")
        String description,
        @NotNull(message = "Product's cost price is required")
        @Positive(message = "Product's cost price must be a positive number")
        BigDecimal costPrice,
        @Nullable
        MultipartFile image,
        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be a positive number")
        Long categoryId,
        @NotNull(message = "Current stock is required")
        @Positive(message = "Current stock must be a positive number")
        Integer currentStock,
        @NotNull(message = "Minimum stock is required")
        @Positive(message = "Minimum stock must be a positive number")
        Integer minStock
) {
}
