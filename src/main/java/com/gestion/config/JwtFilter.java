package com.gestion.config;

import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.service.JwtService.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserPrincipalMapper userPrincipalMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {
        String token = null;
        if (request.getCookies() != null) {
            // .stream() nos permite crear un flujo de datos para poder trabajar de forma individual con cada elemento de un array
            token = Arrays.stream(request.getCookies())
                    // nos quedamos solo con la cookie
                    .filter(cookie -> "token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);

        }

        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{'error': 'Unauthorized: Missing token'}");
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("aplication/json");
            response.getWriter().write("{'error': 'Unauthorized: Invalid token'}");
        }

        // En este punto el token existe y es valido
        Claims tokenClaims = jwtService.getClaimsFromToken(token);
        UserPrincipal userPrincipal = userPrincipalMapper.toEntity(tokenClaims);

        // esta clase es la forma en la que springboot encapsula información de sesión, credenciales y roles, para luego inyectarlo en el security context.
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // una vez hemos cargado la sesión dejamos que siga el flujo normal
        filterChain.doFilter(request, response);

    }

}
