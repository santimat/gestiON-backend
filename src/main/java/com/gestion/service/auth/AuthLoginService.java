package com.gestion.service.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.exception.InactiveResourceException;
import com.gestion.exception.WrongPasswordException;
import com.gestion.mappers.TokenPayloadMapper;
import com.gestion.model.User;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserFinderByEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthLoginService {
    private final UserFinderByEmailService userFinderByEmailService;
    private final AuthCheckerStatusService authCheckerStatusService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(UserLoginRequest userRequest) {

        User loginUser = userFinderByEmailService.findByEmail(userRequest.email());

        if (loginUser.getCommerce() != null && !authCheckerStatusService.isCommerceUserActive(loginUser.getEmail()))
            throw new InactiveResourceException("Commerce user is not active");

        boolean passwordMatches = passwordEncoder.matches(userRequest.password(), loginUser.getPassword());
        if (!passwordMatches) throw new WrongPasswordException("Wrong password");

        return jwtService.generateToken(TokenPayloadMapper.toTokenPayload(loginUser));
    }
}
