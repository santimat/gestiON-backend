package com.gestion.config;

import com.gestion.dto.project.user.UserSessionSnapshot;
import com.gestion.dto.response.exception.ExceptionResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserSessionSnapshotFinderService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserSessionSnapshotFinderService userSessionSnapshotFinderService;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtService jwtService,
                     UserSessionSnapshotFinderService userSessionSnapshotFinderService,
                     ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userSessionSnapshotFinderService = userSessionSnapshotFinderService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {

        Cookie cookie = WebUtils.getCookie(request, "token");

        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            Long userId = extractUserId(cookie.getValue());

            // token invalido, vencido o sin userId: se sigue sin autenticar y el entry point de
            // seguridad responde 401 si el endpoint lo requiere
            if (userId != null) {
                Optional<UserSessionSnapshot> snapshot = userSessionSnapshotFinderService
                        .findSessionSnapshotByUserId(userId);

                if (snapshot.isEmpty()) {
                    // el usuario ya no existe: el token es de una cuenta borrada
                    writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
                    return;
                }

                if (!snapshot.get().isSessionValid()) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.ACCOUNT_DISABLED);
                    return;
                }

                UserPrincipal userPrincipal = UserPrincipalMapper.toEntity(snapshot.get());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // una vez hemos cargado la sesión dejamos que siga el flujo normal
        filterChain.doFilter(request, response);
    }

    // verifica firma y expiracion una sola vez y devuelve el userId, o null si el token no sirve
    private Long extractUserId(String token) {
        try {
            Claims claims = jwtService.getClaimsFromToken(token);
            return claims.get("userId", Long.class);
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    private void writeError(HttpServletResponse response, int status, ErrorCode errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                new ExceptionResponse(status, errorCode.name(), errorCode.getDefaultMessage())
        );
    }

}
