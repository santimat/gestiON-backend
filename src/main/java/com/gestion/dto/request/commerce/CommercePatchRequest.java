package com.gestion.dto.request.commerce;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

public record CommercePatchRequest(
        @Nullable
        @Length(min = 2, max = 100, message = "Name's length must be between 2 and 100")
        String name,

        @Nullable
        @Positive(message = "Profit multiplier must be a positive number")
        Double profitMultiplier,

        @Nullable
        MultipartFile image
) {
}