package com.gestion.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ClientRequest(
        @NotBlank
        @Length(min = 2, max = 20, message = "Name's length must be between 2 and 20 characters")
        String name,
        @NotBlank
        @Length(min = 2, max = 20, message = "Last name's length must be between 2 and 20 characters")
        String lastName,
        @NotBlank
        @Length(min = 2, max = 50, message = "Address's length must be between 2 and 50 characters")
        String address,
        @NotBlank
        @Length(min = 2, max = 20, message = "Phone number's length must be between 2 and 20 characters")
        String phoneNumber,

        @NotBlank
        @Length(min = 2, max = 20, message = "DNI's length must be between 2 and 20 characters")
        String dni
) {

}

