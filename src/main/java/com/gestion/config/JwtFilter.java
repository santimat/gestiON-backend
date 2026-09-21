package com.gestion.config;

import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.service.jwt.JwtService;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {

        Cookie cookie = WebUtils.getCookie(request, "token");


        if (cookie != null && StringUtils.hasText(cookie.getValue()) && jwtService.isTokenValid(cookie.getValue())) {
            Claims claims = jwtService.getClaimsFromToken(cookie.getValue());
            UserPrincipal userPrincipal = UserPrincipalMapper.toEntity(claims);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal,
                    null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // una vez hemos cargado la sesión dejamos que siga el flujo normal
        filterChain.doFilter(request, response);
    }

}
