package com.gestion.dto.response.user;

import com.gestion.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active,
        String phoneNumber,
        LocalDateTime createdAt
) {
}
