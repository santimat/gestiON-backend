package com.gestion.config;

import com.gestion.dto.project.user.UserActiveStatus;
import com.gestion.enums.ErrorCode;
import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.properties.AuthCookieProperties;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserActiveStatusFinderService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

/**
 * Solo se agrega a la cadena de Spring Security; el auto-registro como filtro del servlet container
 * esta desactivado en SecurityConfig.
 */
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserActiveStatusFinderService userActiveStatusFinderService;
    private final SecurityErrorWriter securityErrorWriter;
    private final AuthCookieProperties cookieProperties;

    // el login es publico y no necesita principal: si corriera, una cookie vieja de un usuario
    // borrado o revocado devolveria 401 en el propio login e impediria volver a loguearse
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && LoginRateLimitFilter.LOGIN_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {

        Cookie cookie = WebUtils.getCookie(request, cookieProperties.name());

        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            ParsedToken parsedToken = parseToken(cookie.getValue());

            // token invalido, vencido o sin los claims obligatorios: se sigue sin autenticar y el
            // entry point de seguridad responde 401 si el endpoint lo requiere
            if (parsedToken != null) {
                UserPrincipal userPrincipal = parsedToken.principal();

                UserActiveStatus activeStatus = userActiveStatusFinderService
                        .findActiveStatusByUserId(userPrincipal.getId());

                if (activeStatus == null) {
                    // el usuario ya no existe: el token es de una cuenta borrada
                    securityErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                // el usuario cambio (contraseña, email, rol, punto de venta) o hizo logout de todos
                // los dispositivos despues de emitirse este token: la sesion ya no es valida
                if (activeStatus.version() != parsedToken.version()) {
                    securityErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                if (!activeStatus.isSessionValid()) {
                    securityErrorWriter.write(response, ErrorCode.ACCOUNT_DISABLED);
                    return;
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // una vez hemos cargado la sesión dejamos que siga el flujo normal
        filterChain.doFilter(request, response);
    }

    private record ParsedToken(UserPrincipal principal, long version) {
    }

    // verifica firma y expiracion una sola vez y exige los claims que el principal necesita: un
    // token sin ellos (p. ej. emitido antes de agregar "ver") o con un rol desconocido se trata como
    // invalido, en vez de dejar escapar un NPE / IllegalArgumentException como 500
    private ParsedToken parseToken(String token) {
        try {
            Claims claims = jwtService.getClaimsFromToken(token);
            Long version = claims.get(JwtService.VERSION_CLAIM, Long.class);
            if (version == null
                    || claims.get("userId", Long.class) == null
                    || claims.get("role", String.class) == null) {
                return null;
            }
            return new ParsedToken(UserPrincipalMapper.toEntity(claims), version);
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}
