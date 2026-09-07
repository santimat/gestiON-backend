package com.gestion.dto.request.client;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ClientRequest(
        @NotBlank
        @Length(min = 2, max = 20)
        String name,
        @NotBlank
        @Length(min = 2, max = 20)
        String lastName,
        @NotBlank
        @Length(min = 2, max = 50)
        String address,
        @NotBlank
        @Length(min = 2, max = 20)
        String phoneNumber,
        
        @NotBlank
        @Length(min = 2, max = 20)
        String dni
) {

}

