package com.gestion.controller.auth;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.jwt.JwtService;
import com.gestion.utils.TokenPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El flag active de un usuario tiene que cortar el acceso:
 *
 * - en el login, un usuario desactivado no puede iniciar sesion (403 ACCOUNT_DISABLED);
 * - en cada request, un token emitido antes de la desactivacion deja de servir (403
 *   ACCOUNT_DISABLED), porque el JWT vive 24 horas y por si solo no refleja el estado actual
 *   de la cuenta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeactivatedUserAccessIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaCommerceRepository commerceRepository;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("un usuario desactivado no puede iniciar sesion")
    void deactivatedUserCannotLogIn() throws Exception {
        User user = persistUser(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"secreto123"}
                                """.formatted(user.getEmail())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DISABLED.name()));
    }

    @Test
    @DisplayName("un token emitido antes de desactivar el usuario deja de servir")
    void tokenIssuedBeforeDeactivationStopsWorking() throws Exception {
        User user = persistUser(true);
        String token = tokenFor(user);

        // el token sirve mientras el usuario esta activo
        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("token", token)))
                .andExpect(status().isOk());

        user.setActive(false);
        userRepository.save(user);

        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("token", token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DISABLED.name()));
    }

    @Test
    @DisplayName("un token de un usuario que ya no existe se rechaza como no autorizado")
    void tokenOfDeletedUserIsUnauthorized() throws Exception {
        User user = persistUser(true);
        String token = tokenFor(user);

        userRepository.delete(user);

        mockMvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("token", token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.name()));
    }

    private User persistUser(boolean active) {
        Commerce commerce = commerceRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("no hay comercios en la base"));

        User user = new User();
        user.setName("Usuario de prueba");
        user.setEmail("desactivado-" + System.nanoTime() + "@gestion.local");
        user.setPassword(passwordEncoder.encode("secreto123"));
        user.setRole(Role.CASHIER);
        user.setActive(active);
        user.setCommerce(commerce);
        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        TokenPayload payload = new TokenPayload(
                user.getEmail(),
                user.getName(),
                user.getId(),
                user.getCommerce() != null ? user.getCommerce().getId() : null,
                user.getRole(),
                true,
                null);
        return jwtService.generateToken(payload);
    }
}
