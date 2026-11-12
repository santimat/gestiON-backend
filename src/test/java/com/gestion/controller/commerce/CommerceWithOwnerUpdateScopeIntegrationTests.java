package com.gestion.controller.commerce;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.properties.AdminProperties;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PUT /api/commerces/{commerceId}/user/{userId} actualiza el comercio y su owner.
 *
 * El userId viene del path y no de un token, asi que sin un finder scopeado por comercio se
 * podrian actualizar usuarios de otro comercio. Los ids de otro comercio tienen que responder
 * 404 USER_NOT_FOUND, nunca 403.
 *
 * Ademas, el email, el cuit y la direccion son unicos: la violacion tiene que reportarse con el
 * codigo especifico y no con un DATA_INTEGRITY_VIOLATION de la base.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CommerceWithOwnerUpdateScopeIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JpaCommerceRepository commerceRepository;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AdminProperties adminProperties;

    @Test
    @DisplayName("actualizar el owner de otro comercio devuelve 404 USER_NOT_FOUND")
    void updatingOwnerOfAnotherCommerceReturnsNotFound() throws Exception {
        Commerce ownerCommerce = persistCommerce();
        Commerce otherCommerce = persistCommerce();
        User otherOwner = persistOwner(otherCommerce);

        mockMvc.perform(putAsSudo(ownerCommerce.getId(), otherOwner.getId())
                        .param("username", "Owner Actualizado")
                        .param("email", uniqueEmail())
                        .param("phoneNumber", "1234567890")
                        .param("name", "Comercio Actualizado")
                        .param("address", uniqueAddress())
                        .param("cuit", uniqueCuit()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()));

        // el owner del otro comercio no fue tocado
        User reloaded = userRepository.findById(otherOwner.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo(otherOwner.getName());
        assertThat(reloaded.getEmail()).isEqualTo(otherOwner.getEmail());
    }

    @Test
    @DisplayName("un email que ya existe devuelve 409 EMAIL_ALREADY_EXISTS")
    void duplicateEmailReturnsConflict() throws Exception {
        Commerce commerce = persistCommerce();
        User owner = persistOwner(commerce);
        // un segundo owner del mismo comercio que ya tiene el email que se quiere reutilizar
        String takenEmail = uniqueEmail();
        persistOwnerWith(commerce, takenEmail);

        mockMvc.perform(putAsSudo(commerce.getId(), owner.getId())
                        .param("username", "Owner Actualizado")
                        .param("email", takenEmail)
                        .param("phoneNumber", "1234567890")
                        .param("name", "Comercio Actualizado")
                        .param("address", uniqueAddress())
                        .param("cuit", uniqueCuit()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.name()));
    }

    @Test
    @DisplayName("un cuit que ya existe devuelve 409 COMMERCE_CUIT_ALREADY_EXISTS")
    void duplicateCuitReturnsConflict() throws Exception {
        Commerce commerce = persistCommerce();
        User owner = persistOwner(commerce);
        // un segundo comercio que se queda con el cuit que se quiere reutilizar
        String takenCuit = uniqueCuit();
        persistCommerceWith(takenCuit, uniqueAddress());

        mockMvc.perform(putAsSudo(commerce.getId(), owner.getId())
                        .param("username", "Owner Actualizado")
                        .param("email", uniqueEmail())
                        .param("phoneNumber", "1234567890")
                        .param("name", "Comercio Actualizado")
                        .param("address", uniqueAddress())
                        .param("cuit", takenCuit))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMERCE_CUIT_ALREADY_EXISTS.name()));
    }

    @Test
    @DisplayName("una direccion que ya existe devuelve 409 COMMERCE_ADDRESS_ALREADY_EXISTS")
    void duplicateAddressReturnsConflict() throws Exception {
        Commerce commerce = persistCommerce();
        User owner = persistOwner(commerce);
        // un segundo comercio que se queda con la direccion que se quiere reutilizar
        String takenAddress = uniqueAddress();
        persistCommerceWith(uniqueCuit(), takenAddress);

        mockMvc.perform(putAsSudo(commerce.getId(), owner.getId())
                        .param("username", "Owner Actualizado")
                        .param("email", uniqueEmail())
                        .param("phoneNumber", "1234567890")
                        .param("name", "Comercio Actualizado")
                        .param("address", takenAddress)
                        .param("cuit", uniqueCuit()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMERCE_ADDRESS_ALREADY_EXISTS.name()));
    }

    @Test
    @DisplayName("un owner puede guardar sus propios datos sin cambiar el email")
    void ownerCanKeepItsOwnEmail() throws Exception {
        Commerce commerce = persistCommerce();
        User owner = persistOwner(commerce);

        mockMvc.perform(putAsSudo(commerce.getId(), owner.getId())
                        .param("username", "Owner Actualizado")
                        .param("email", owner.getEmail())
                        .param("phoneNumber", "1234567890")
                        .param("name", "Comercio Actualizado")
                        .param("address", uniqueAddress())
                        .param("cuit", commerce.getCuit()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(owner.getEmail()))
                .andExpect(jsonPath("$.commerceId").value(commerce.getId()));
    }

    /**
     * El endpoint bindea con @ModelAttribute y tiene un MultipartFile, asi que la request tiene
     * que ir como multipart/form-data: con parametros sueltos la ruta no matchea y responde
     * 404 ENDPOINT_NOT_FOUND.
     */
    private MockMultipartHttpServletRequestBuilder putAsSudo(Long commerceId, Long userId) {
        return (MockMultipartHttpServletRequestBuilder)
                multipart(HttpMethod.PUT, "/api/commerces/{commerceId}/user/{userId}", commerceId, userId)
                        .with(asSudo());
    }

    /**
     * El endpoint es de SUDO: el admin se crea solo al arrancar la app, asi que se autentica
     * con el usuario real de la base en vez de un principal falso.
     */
    private UserPrincipal sudoPrincipal() {
        User sudo = userRepository.findByEmail(adminProperties.email()).orElseThrow();
        return new UserPrincipal(sudo.getId(), null, sudo.getEmail(), sudo.getName(), Role.SUDO, true, null);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor asSudo() {
        UserPrincipal principal = sudoPrincipal();
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    // cuit: la columna admite 12 caracteres y el request exige entre 8 y 20. el sufijo del
    // nanotime evita chocar con los datos que quedaron de corridas anteriores.
    private String uniqueCuit() {
        return "C" + String.format("%09d", Math.abs(System.nanoTime() % 1_000_000_000L));
    }

    private String uniqueAddress() {
        return "Address " + System.nanoTime() + " - direccion unica de prueba";
    }

    private String uniqueEmail() {
        return "user" + System.nanoTime() + "@gestion.local";
    }

    private Commerce persistCommerce() {
        return persistCommerceWith(uniqueCuit(), uniqueAddress());
    }

    private Commerce persistCommerceWith(String cuit, String address) {
        Commerce commerce = new Commerce();
        commerce.setName("Comercio " + System.nanoTime());
        commerce.setCuit(cuit);
        commerce.setAddress(address);
        commerce.setActive(true);
        commerce.setProfitMultiplier(1.0);
        return commerceRepository.save(commerce);
    }

    private User persistOwner(Commerce commerce) {
        return persistOwnerWith(commerce, uniqueEmail());
    }

    private User persistOwnerWith(Commerce commerce, String email) {
        User owner = new User();
        owner.setName("Owner " + System.nanoTime());
        owner.setEmail(email);
        owner.setPassword(passwordEncoder.encode("secreto123"));
        owner.setRole(Role.OWNER);
        owner.setActive(true);
        owner.setCommerce(commerce);
        return userRepository.save(owner);
    }
}
