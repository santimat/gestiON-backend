package com.gestion.utils;

import com.gestion.enums.Role;

public record TokenPayload(
        String email,
        Long userId,
        Role role
) {
}
