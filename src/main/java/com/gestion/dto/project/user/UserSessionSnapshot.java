package com.gestion.dto.project.user;

import com.gestion.enums.Role;

public record UserSessionSnapshot(
        Long userId,
        String email,
        String name,
        Role role,
        boolean userActive,
        Long commerceId,
        Boolean commerceActive,
        Long sellPointId
) {

    // commerceActive es null para SUDO (no tiene comercio)
    public boolean isSessionValid() {
        if (!userActive) {
            return false;
        }
        return commerceActive == null || commerceActive;
    }
}
