package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.repository.JpaClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los endpoints paginados armaban el PageRequest a mano, sin ningun tope: con ?size=1000000 un
 * endpoint se convertible en un dump de la tabla, y la query agregada del resumen de cuenta de
 * los clientes expande IN :clientIds a un placeholder por id, con el corte de MySQL en 65.535.
 *
 * Los parametros fuera de rango se rechazan con un 400 INVALID_PARAMETER, no se clampean en
 * silencio: el cliente tiene que saber que lo que pidio no se puede servir.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PaginationParametersIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JpaClientRepository clientRepository;

    @Test
    @DisplayName("un size de cero devuelve 400 INVALID_PARAMETER")
    void zeroSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("size", "0")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.name()))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("un size por encima del maximo devuelve 400 INVALID_PARAMETER")
    void sizeAboveMaximumReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("size", "1000000")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.name()));
    }

    @Test
    @DisplayName("un page negativo devuelve 400 INVALID_PARAMETER")
    void negativePageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("page", "-1")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.name()));
    }

    @Test
    @DisplayName("un sortBy desconocido devuelve 400 INVALID_PARAMETER y no 500")
    void unknownSortPropertyReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("sortBy", "columnaInexistente")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.name()));
    }

    @Test
    @DisplayName("un sortDir invalido devuelve 400 INVALID_PARAMETER")
    void invalidSortDirectionReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("sortDir", "lateral")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.name()));
    }

    @Test
    @DisplayName("un size dentro del rango se sirve con normalidad")
    void sizeWithinRangeIsServed() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("size", "100")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal(), null, principal().getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private UserPrincipal principal() {
        return new UserPrincipal(1L, 1L, "owner@gestion.local", "Owner", Role.OWNER, true, null);
    }
}
