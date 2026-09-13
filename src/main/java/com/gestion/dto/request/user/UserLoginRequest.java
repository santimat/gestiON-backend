package com.gestion.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequest(
        @Email
        @NotBlank
        @Length(min = 5, max = 70, message = "Email's length must be between 5 and 70")
        String email,
        @NotBlank
        @Length(min = 5, max = 50, message = "Password's length must be between 3 and 50")
        String password
) {
}
