package com.gestion.dto.response.user;

import com.gestion.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        boolean active,
        String phoneNumber,
        LocalDateTime createdAt
) {
}
