package com.gestion.service.auth;

import com.gestion.model.Commerce;
import com.gestion.service.user.UserFinderByEmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthCheckerStatusService {
    private final UserFinderByEmailService userFinderByEmailService;

    public boolean isCommerceUserActive(String email) {
        Commerce commerce = userFinderByEmailService.findByEmail(email).getCommerce();
        return commerce == null || commerce.isActive();
    }
}
