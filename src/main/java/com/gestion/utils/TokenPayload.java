package com.gestion.utils;

import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;

public record TokenPayload(
        String email,
        String name,
        Long userId,
        Role role,
        UserStatus status
) {
}
