package com.gestion.dto.response.user;

import com.gestion.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        String phoneNumber,
        Instant updatedAt
) {
}
