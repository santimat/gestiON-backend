package com.gestion.dto.project.user;


public record UserActiveStatus(
        boolean userActive,
        Boolean commerceActive
) {
    
    public boolean isSessionValid() {
        if (!userActive) {
            return false;
        }
        return commerceActive == null || commerceActive;
    }
}
