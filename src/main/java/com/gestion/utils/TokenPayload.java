package com.gestion.utils;

import com.gestion.enums.Role;

public record TokenPayload(
        String email,
        String name,
        Long userId,
        Role role
) {
}
