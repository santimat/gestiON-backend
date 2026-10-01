package com.gestion.dto.request.commerce;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

public record CommerceWithOwnerUpdateRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 50, message = "Name's length must be between 2 and 50")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email's format is invalid")
        String email,

        @NotBlank
        @Length(min = 10, max = 20, message = "Phone Number's length must be between 10 and 20")
        String phoneNumber,

        @NotBlank(message = "Business Name is required")
        @Length(min = 3, max = 100, message = "Business Name's length must be between 3 and 100")
        String businessName,

        @NotBlank
        @Length(min = 3, max = 100, message = "Address' length must be between 3 and 100")
        String address,

        @NotBlank
        @Length(min = 8, max = 20, message = "CUIT's length must be between 8 and 20")
        String cuit,

        @Nullable
        MultipartFile businessLogo
) {
}
