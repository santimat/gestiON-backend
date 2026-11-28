package com.gestion.dto.request.commerce;


import jakarta.annotation.Nullable;
import com.gestion.utils.CuitNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

public record CommerceRequest(
        @NotBlank
        @Length(min = 2, max = 100, message = "The name must be between 2 and 100 characters")
        String name,
        @NotBlank
        @Length(min = 2, max = 100, message = "The address must be between 2 and 100 characters")
        String address,
        @NotBlank
        @Pattern(regexp = CuitNormalizer.PATTERN, message = CuitNormalizer.MESSAGE)
        String cuit,
        @Nullable
        MultipartFile logo,
        @Nullable
        @Positive
        Double profitMultiplier
) {
    // el binding (JSON o multipart) construye el record por este constructor: la validacion
    // @Pattern corre sobre el CUIT ya normalizado (solo digitos)
    public CommerceRequest {
        cuit = CuitNormalizer.normalize(cuit);
    }
}
