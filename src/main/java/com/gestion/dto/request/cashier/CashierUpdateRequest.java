package com.gestion.dto.request.cashier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.gestion.validation.PasswordPolicy;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record CashierUpdateRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name's length must be between 2 and 50")
        String name,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        // opcional: null o vacio = no cambiar la contraseña
        @PasswordPolicy
        String password,

        @Length(min = 10, max = 20, message = "Phone number's length must be between 10 and 20 characters")
        String phoneNumber
) {
}
