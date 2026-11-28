package com.gestion.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @Email
        @NotBlank
        String email,
        // sin reglas de largo: no se revela la politica ni se rechazan contraseñas creadas con una
        // politica anterior. AuthLoginService trata las de mas de 72 bytes como credenciales invalidas.
        @NotBlank
        String password
) {
}
