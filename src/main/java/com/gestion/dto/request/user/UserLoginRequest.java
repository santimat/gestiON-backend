package com.gestion.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        @Length(min = 3, max = 50, message = "Password's length must be between 3 and 50")
        String password
) {
}
