package com.gestion.dto.request.commerce;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

public record CommerceRequest(
        @NotBlank
        @Length(min = 2, max = 100, message = "The business name must be between 2 and 100 characters")
        String businessName,
        @NotBlank
        @Length(min = 2, max = 100, message = "The address must be between 2 and 100 characters")
        String address,
        @NotBlank
        @Length(min = 8, max = 20, message = "The CUIT must be between 8 and 20 characters")
        String cuit,
        @Nullable
        MultipartFile logo,
        @Nullable
        @Positive
        Double profitMultiplier
) {
}
