package com.gestion.dto.request.user;

import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name's length must be between 2 and 50")
        String name,

        @Email
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 3, max = 50, message = "Password's length must be between 3 and 50")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        @NotNull(message = "User status is required")
        UserStatus status
) {
}
