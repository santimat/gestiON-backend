package com.gestion.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record UserUpdateRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name's length must be between 2 and 50")
        String name,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @Length(min = 10, max = 20, message = "Phone number's length must be between 10 and 20 characters")
        String phoneNumber
) {
}
