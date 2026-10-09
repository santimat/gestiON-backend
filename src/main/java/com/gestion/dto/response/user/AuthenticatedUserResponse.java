package com.gestion.dto.response.user;

import com.gestion.enums.Role;

public record AuthenticatedUserResponse(
        Long id,
        Long commerceId,
        String name,
        String email,
        Role role,
        Boolean active) {
}
