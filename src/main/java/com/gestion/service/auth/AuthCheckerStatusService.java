package com.gestion.service.auth;

import com.gestion.service.user.UserFinderByEmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthCheckerStatusService {
    private final UserFinderByEmailService userFinderByEmailService;

    public boolean isCommerceUserActive(String email) {
        return userFinderByEmailService.findByEmail(email).getCommerce().isActive();
    }
}
