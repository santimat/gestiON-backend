package com.gestion.config;

import com.gestion.dto.project.user.UserSessionSnapshot;
import com.gestion.enums.ErrorCode;
import com.gestion.mappers.UserPrincipalMapper;
import com.gestion.properties.AuthCookieProperties;
import com.gestion.service.jwt.JwtService;
import com.gestion.service.user.UserSessionSnapshotFinderService;
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
import java.util.Optional;

/**
 * Solo se agrega a la cadena de Spring Security; el auto-registro como filtro del servlet container
 * esta desactivado en SecurityConfig.
 */
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserSessionSnapshotFinderService userSessionSnapshotFinderService;
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
            TokenIdentity tokenIdentity = parseToken(cookie.getValue());

            // token invalido, vencido o sin userId/ver: se sigue sin autenticar y el entry point de
            // seguridad responde 401 si el endpoint lo requiere
            if (tokenIdentity != null) {
                // el token solo identifica al usuario: rol, comercio, punto de venta y estado activo
                // se leen de la base en cada request (una proyeccion minima, sin cargar entidades).
                // Asi un usuario desactivado, o al que le cambiaron el rol o el punto de venta, ve el
                // cambio en el siguiente request en vez de conservar los datos del login hasta que
                // el token expire.
                Optional<UserSessionSnapshot> snapshot = userSessionSnapshotFinderService
                        .findSessionSnapshotByUserId(tokenIdentity.userId());

                if (snapshot.isEmpty()) {
                    // el usuario ya no existe: el token es de una cuenta borrada
                    securityErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                // el usuario cambio (p. ej. su contraseña) o hizo logout de todos los dispositivos
                // despues de emitirse este token: la sesion ya no es valida
                if (snapshot.get().version() != tokenIdentity.version()) {
                    securityErrorWriter.write(response, ErrorCode.UNAUTHORIZED);
                    return;
                }

                if (!snapshot.get().isSessionValid()) {
                    securityErrorWriter.write(response, ErrorCode.ACCOUNT_DISABLED);
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

    private record TokenIdentity(Long userId, long version) {
    }

    // verifica firma y expiracion una sola vez y exige los claims que el filtro necesita: un token
    // sin ellos (p. ej. emitido antes de agregar "ver") se trata como invalido
    private TokenIdentity parseToken(String token) {
        try {
            Claims claims = jwtService.getClaimsFromToken(token);
            Long userId = claims.get("userId", Long.class);
            Long version = claims.get(JwtService.VERSION_CLAIM, Long.class);
            if (userId == null || version == null) {
                return null;
            }
            return new TokenIdentity(userId, version);
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}
