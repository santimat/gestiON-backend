package com.gestion.service.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.exception.InvalidCredentialsException;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.mappers.TokenPayloadMapper;
import com.gestion.model.User;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserFinderByEmailService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthLoginService {
    private final UserFinderByEmailService userFinderByEmailService;
    private final AuthCheckerStatusService authCheckerStatusService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(UserLoginRequest userRequest) {

        User loginUser;
        try {
            loginUser = userFinderByEmailService.findByEmail(userRequest.email());
        } catch (ResourceNotFoundException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        boolean passwordMatches = passwordEncoder.matches(userRequest.password(), loginUser.getPassword());
        if (!passwordMatches)
            throw new InvalidCredentialsException("Invalid email or password");

        if (loginUser.getCommerce() != null && !authCheckerStatusService.isCommerceUserActive(loginUser.getEmail()))
            throw new InactiveResourceException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled");

        return jwtService.generateToken(TokenPayloadMapper.toTokenPayload(loginUser));
    }
}
