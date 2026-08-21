package com.gestion.dto.response.user;

import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status) {
}
