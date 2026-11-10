package com.gestion.controller.client;

import com.gestion.config.UserPrincipal;
import com.gestion.controller.client.ClientGetController;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.enums.Role;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El resumen de cuenta corriente (salesCount, pendingSalesCount, totalSpent, totalPending) tiene que
 * venir calculado en TODOS los endpoints que devuelven un cliente, con los mismos valores que la
 * suma real de sus ventas, y sin que el listado dispare una query por cliente.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
class ClientAccountSummaryIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JpaClientRepository clientRepository;

    @Autowired
    ClientGetController clientGetController;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void clientByDniReturnsAccountSummary() throws Exception {
        Client client = clientRepository.findAll().stream()
                .filter(c -> c.getDni() != null && !c.getDni().isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no hay clientes con DNI en la base"));

        mockMvc.perform(get("/api/clients/{dni}", client.getDni())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principalFor(client), null, principalFor(client).getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesCount").isNumber())
                .andExpect(jsonPath("$.pendingSalesCount").isNumber())
                .andExpect(jsonPath("$.totalSpent").isNumber())
                .andExpect(jsonPath("$.totalPending").isNumber());
    }

    @Test
    void accountSummaryMatchesTheSumOfItsSales() {
        Object[] clientWithSales = (Object[]) entityManager
                .createNativeQuery("SELECT client_id, commerce_id FROM sales "
                        + "WHERE client_id IS NOT NULL AND status = 'PAYED' GROUP BY client_id, commerce_id LIMIT 1")
                .getSingleResult();
        Long clientId = ((Number) clientWithSales[0]).longValue();
        Long commerceId = ((Number) clientWithSales[1]).longValue();

        Number expectedPaidCount = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM sales WHERE client_id = ?1 AND commerce_id = ?2 AND status = 'PAYED'")
                .setParameter(1, clientId).setParameter(2, commerceId).getSingleResult();
        BigDecimal expectedSpent = (BigDecimal) entityManager
                .createNativeQuery("SELECT COALESCE(SUM(total), 0) FROM sales WHERE client_id = ?1 AND commerce_id = ?2 AND status = 'PAYED'")
                .setParameter(1, clientId).setParameter(2, commerceId).getSingleResult();

        ClientResponse response = getClientByDni(dniOf(clientId), principalFor(clientId, commerceId));

        assertEquals(expectedPaidCount.longValue(), response.salesCount().longValue(),
                "salesCount tiene que coincidir con la cantidad de ventas pagadas");
        assertEquals(0, expectedSpent.compareTo(response.totalSpent()),
                "totalSpent tiene que coincidir con la suma de los totales de las ventas pagadas");
    }

    @Test
    void clientWithoutSalesGetsZeroes() {
        Object[] clientWithoutSales = (Object[]) entityManager
                .createNativeQuery("SELECT c.id, c.commerce_id FROM clients c "
                        + "LEFT JOIN sales s ON s.client_id = c.id WHERE s.id IS NULL LIMIT 1")
                .getSingleResult();
        Long clientId = ((Number) clientWithoutSales[0]).longValue();
        Long commerceId = ((Number) clientWithoutSales[1]).longValue();

        ClientResponse response = getClientByDni(dniOf(clientId), principalFor(clientId, commerceId));

        assertNotNull(response.salesCount());
        assertNotNull(response.pendingSalesCount());
        assertNotNull(response.totalSpent());
        assertNotNull(response.totalPending());
        assertEquals(0L, response.salesCount().longValue());
        assertEquals(0L, response.pendingSalesCount().longValue());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.totalSpent()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.totalPending()));
    }

    @Test
    void clientListDoesNotScaleQueriesWithPageSize() throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

        long smallPageQueries = countQueries(statistics, 5);
        long bigPageQueries = countQueries(statistics, 20);

        assertEquals(smallPageQueries, bigPageQueries,
                "el listado no puede disparar una query por cliente (small=" + smallPageQueries
                        + ", big=" + bigPageQueries + ")");
        assertTrue(smallPageQueries <= 5,
                "el listado deberia resolver el resumen con una sola query (fueron " + smallPageQueries + ")");
    }

    private long countQueries(Statistics statistics, int size) throws Exception {
        statistics.clear();
        mockMvc.perform(get("/api/clients")
                        .param("size", String.valueOf(size))
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principalFor(clientRepository.findAll().get(0)), null,
                                principalFor(clientRepository.findAll().get(0)).getAuthorities()))))
                .andExpect(status().isOk());
        return statistics.getQueryExecutionCount();
    }

    private ClientResponse getClientByDni(String dni, UserPrincipal principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        try {
            return clientGetController.getClientByDni(dni, principal).getBody();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private UserPrincipal principalFor(Client client) {
        return principalFor(client.getId(), client.getCommerce().getId());
    }

    private UserPrincipal principalFor(Long clientId, Long commerceId) {
        return new UserPrincipal(99L, commerceId, "test@gestion.local", "Test", Role.OWNER, true, null);
    }

    private String dniOf(Long clientId) {
        return clientRepository.findById(clientId).orElseThrow().getDni();
    }
}
