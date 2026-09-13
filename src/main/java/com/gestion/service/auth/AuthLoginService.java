package com.gestion.service.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.exception.WrongPasswordException;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(UserLoginRequest userRequest) {
        User loginUser = userFinderByEmailService.findByEmail(userRequest.email());

        boolean passwordMatches = passwordEncoder.matches(userRequest.password(), loginUser.getPassword());
        if (!passwordMatches) throw new WrongPasswordException("Wrong password");

        return jwtService.generateToken(TokenPayloadMapper.toTokenPayload(loginUser));
    }
}
