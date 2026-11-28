package com.gestion.service.auth;

import com.gestion.dto.request.user.UserLoginRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.exception.InvalidCredentialsException;
import com.gestion.exception.TooManyLoginAttemptsException;
import com.gestion.mappers.TokenPayloadMapper;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.jwt.JwtService;
import com.gestion.validation.PasswordRules;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthLoginService {
    private final JpaUserRepository userRepository;
    private final AuthCheckerStatusService authCheckerStatusService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthLoginAttemptLimiterService loginAttemptLimiterService;

    // hash contra el que se compara cuando el email no existe: asi el login tarda lo mismo (un BCrypt)
    // exista o no la cuenta, y el tiempo de respuesta no permite enumerar usuarios
    private final String dummyPasswordHash;

    public AuthLoginService(JpaUserRepository userRepository,
                            AuthCheckerStatusService authCheckerStatusService,
                            PasswordEncoder passwordEncoder,
                            JwtService jwtService,
                            AuthLoginAttemptLimiterService loginAttemptLimiterService) {
        this.userRepository = userRepository;
        this.authCheckerStatusService = authCheckerStatusService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptLimiterService = loginAttemptLimiterService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-equalization");
    }

    public String login(UserLoginRequest userRequest) {
        // bloqueo por cuenta: N fallos seguidos dentro de la ventana. Se chequea antes de BCrypt y
        // aplica igual a emails inexistentes, para no revelar cuales existen.
        if (loginAttemptLimiterService.isEmailBlocked(userRequest.email()))
            throw new TooManyLoginAttemptsException();

        Optional<User> loginUser = userRepository.findByEmail(userRequest.email());

        // BCrypt solo considera 72 bytes (y Spring lanza IllegalArgumentException si se excede): una
        // contraseña mas larga nunca puede ser valida, pero igual se paga un BCrypt para igualar tiempos
        String hashToCheck = loginUser.map(User::getPassword).orElse(dummyPasswordHash);
        boolean passwordMatches;
        if (PasswordRules.fitsBcrypt(userRequest.password())) {
            passwordMatches = passwordEncoder.matches(userRequest.password(), hashToCheck);
        } else {
            passwordEncoder.matches("timing-equalization", dummyPasswordHash);
            passwordMatches = false;
        }

        if (loginUser.isEmpty() || !passwordMatches) {
            loginAttemptLimiterService.registerEmailFailure(userRequest.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        loginAttemptLimiterService.resetEmailFailures(userRequest.email());
        User user = loginUser.get();

        if (!user.isActive())
            throw new InactiveResourceException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled");

        if (user.getCommerce() != null && !authCheckerStatusService.isCommerceUserActive(user.getEmail()))
            throw new InactiveResourceException(ErrorCode.ACCOUNT_DISABLED, "The account is disabled");

        return jwtService.generateToken(TokenPayloadMapper.toTokenPayload(user));
    }
}
