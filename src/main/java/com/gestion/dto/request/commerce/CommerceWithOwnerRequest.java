package com.gestion.dto.request.commerce;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import com.gestion.utils.CuitNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

public record CommerceWithOwnerRequest(
        @NotBlank(message = "Name is required")
        @Length(min = 2, max = 50, message = "Name's length must be between 2 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email's format is invalid")
        String email,

        @NotBlank(message = "Password is required")
        @Length(min = 3, message = "Password's length must be at least 3 characters")
        String password,

        @NotBlank
        @Length(min = 10, max = 20, message = "Phone Number's length must be between 10 and 20 characters")
        String phoneNumber,

        @NotBlank(message = "Name is required")
        @Length(min = 3, max = 100, message = "Name's length must be between 3 and 100 characters")
        String name,

        @NotBlank
        @Length(min = 3, max = 100, message = "Address' length must be between 3 and 100 characters")
        String address,

        @NotBlank
        @Pattern(regexp = CuitNormalizer.PATTERN, message = CuitNormalizer.MESSAGE)
        String cuit,

        @Nullable
        MultipartFile businessLogo,

        @Nullable
        @Positive
        Double profitMultiplier
) {
    // el binding (JSON o multipart) construye el record por este constructor: la validacion
    // @Pattern corre sobre el CUIT ya normalizado (solo digitos)
    public CommerceWithOwnerRequest {
        cuit = CuitNormalizer.normalize(cuit);
    }
}
