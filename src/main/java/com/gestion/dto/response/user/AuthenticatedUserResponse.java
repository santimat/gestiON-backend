package com.gestion.dto.response.user;

import com.gestion.enums.Role;

public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Boolean active) {
}
