package com.gestion.dto.project.user;


public record UserActiveStatus(
        boolean userActive,
        Boolean commerceActive,
        long version
) {

    public boolean isSessionValid() {
        if (!userActive) {
            return false;
        }
        return commerceActive == null || commerceActive;
    }
}
