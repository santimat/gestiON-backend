package com.gestion.config;

import com.gestion.dto.project.UserActiveStatus;
import com.gestion.dto.response.exception.ExceptionResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserActiveStatusFinderService;
import io.jsonwebtoken.Claims;
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

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserActiveStatusFinderService userActiveStatusFinderService;
    private final ObjectMapper objectMapper;

    public JwtFilter(JwtService jwtService,
                     UserActiveStatusFinderService userActiveStatusFinderService,
                     ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userActiveStatusFinderService = userActiveStatusFinderService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {

        Cookie cookie = WebUtils.getCookie(request, "token");

        if (cookie != null
                && StringUtils.hasText(cookie.getValue())
                && jwtService.isTokenValid(cookie.getValue())
        ) {
            Claims claims = jwtService.getClaimsFromToken(cookie.getValue());
            UserPrincipal userPrincipal = UserPrincipalMapper.toEntity(claims);

            // el JWT vive 24 horas y el flag active del usuario no se mira despues del login:
            // sin esta revalidacion un usuario desactivado conservaba acceso hasta que expiraba
            // su token. la consulta es una proyeccion minima sobre el id del usuario, no carga
            // la entidad ni sus relaciones.
            UserActiveStatus activeStatus = userActiveStatusFinderService
                    .findActiveStatusByUserId(userPrincipal.getId());

            if (activeStatus == null) {
                // el usuario ya no existe: el token es de una cuenta borrada
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
                return;
            }

            if (!activeStatus.isSessionValid()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.ACCOUNT_DISABLED);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // una vez hemos cargado la sesión dejamos que siga el flujo normal
        filterChain.doFilter(request, response);
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
