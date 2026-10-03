package com.gestion.dto.request.product;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Length(max = 100, message = "Product's length must be lower than 100 characters")
        String name,
        @NotBlank
        @Length(max = 255, message = "Product's length must be lower than 255 characters")
        String description,
        @NotNull
        @Positive
        BigDecimal costPrice,
        @NotNull
        @Positive
        BigDecimal salePrice,
        @Nullable
        MultipartFile image,
        @NotNull
        @Positive
        Long categoryId,
        @NotNull
        @Positive
        Integer currentStock,
        @NotNull
        @Positive
        Integer minStock
) {
}
