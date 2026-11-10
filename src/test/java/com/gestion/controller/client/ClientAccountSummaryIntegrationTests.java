package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.Role;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El resumen de cuenta corriente (salesCount, pendingSalesCount, totalSpent, totalPending) tiene que
 * venirCalculado en TODOS los endpoints que devuelven un cliente, no solo en el listado.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ClientAccountSummaryIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JpaClientRepository clientRepository;

    @Test
    void clientByDniReturnsAccountSummary() throws Exception {
        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getDni() != null && !c.getDni().isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no hay clientes con DNI en la base"));

        UserPrincipal principal = new UserPrincipal(
                99L,
                client.getCommerce().getId(),
                "test@gestion.local",
                "Test",
                Role.OWNER,
                true,
                null);

        mockMvc.perform(get("/api/clients/{dni}", client.getDni())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesCount").isNumber())
                .andExpect(jsonPath("$.pendingSalesCount").isNumber())
                .andExpect(jsonPath("$.totalSpent").isNumber())
                .andExpect(jsonPath("$.totalPending").isNumber());
    }
}
