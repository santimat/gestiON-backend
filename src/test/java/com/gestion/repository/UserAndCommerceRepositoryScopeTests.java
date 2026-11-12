package com.gestion.repository;

import com.gestion.enums.Role;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el comportamiento de las queries derivadas que usa el scoping por comercio y los
 * chequeos de unicidad con exclusion del propio id.
 */
@SpringBootTest
@Transactional
class UserAndCommerceRepositoryScopeTests {

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaCommerceRepository commerceRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("el finder scopeado encuentra al owner de su comercio y no al de otro")
    void scopedFinderMatchesTheCommerceOfTheUser() {
        Commerce commerce = persistCommerce();
        Commerce otherCommerce = persistCommerce();
        User owner = persistOwner(commerce);

        assertThat(userRepository.findByIdAndCommerceIdAndRole(owner.getId(), commerce.getId(), Role.OWNER))
                .isPresent();
        assertThat(userRepository.findByIdAndCommerceIdAndRole(owner.getId(), otherCommerce.getId(), Role.OWNER))
                .isEmpty();
        assertThat(userRepository.findByIdAndCommerceIdAndRole(owner.getId(), commerce.getId(), Role.CASHIER))
                .isEmpty();
    }

    @Test
    @DisplayName("el chequeo de email excluye al propio usuario")
    void emailUniquenessCheckExcludesTheUserItself() {
        Commerce commerce = persistCommerce();
        User owner = persistOwner(commerce);
        User other = persistOwner(commerce);

        assertThat(userRepository.existsByEmailAndIdNot(owner.getEmail(), owner.getId())).isFalse();
        assertThat(userRepository.existsByEmailAndIdNot(owner.getEmail(), other.getId())).isTrue();
    }

    @Test
    @DisplayName("el chequeo de cuit y direccion excluye al propio comercio")
    void commerceUniquenessChecksExcludeTheCommerceItself() {
        Commerce commerce = persistCommerce();
        Commerce other = persistCommerce();

        assertThat(commerceRepository.existsByCuitAndIdNot(commerce.getCuit(), commerce.getId())).isFalse();
        assertThat(commerceRepository.existsByCuitAndIdNot(commerce.getCuit(), other.getId())).isTrue();
        assertThat(commerceRepository.existsByAddressAndIdNot(commerce.getAddress(), commerce.getId())).isFalse();
        assertThat(commerceRepository.existsByAddressAndIdNot(commerce.getAddress(), other.getId())).isTrue();
    }

    @Test
    @DisplayName("el cuit y la direccion se persisten tal como se mandan")
    void cuitAndAddressArePersistedAsSent() {
        Commerce commerce = persistCommerce();

        assertThat(commerce.getCuit()).isNotBlank();
        assertThat(commerce.getAddress()).isNotBlank();
        assertThat(commerceRepository.existsByCuit(commerce.getCuit())).isTrue();
        assertThat(commerceRepository.existsByAddress(commerce.getAddress())).isTrue();
    }

    private Commerce persistCommerce() {
        Commerce commerce = new Commerce();
        commerce.setName("Comercio " + System.nanoTime());
        commerce.setCuit("C" + Math.abs(System.nanoTime() % 1_000_000_000L));
        commerce.setAddress("Address " + System.nanoTime());
        commerce.setActive(true);
        commerce.setProfitMultiplier(1.0);
        return commerceRepository.saveAndFlush(commerce);
    }

    private User persistOwner(Commerce commerce) {
        User owner = new User();
        owner.setName("Owner " + System.nanoTime());
        owner.setEmail("owner" + System.nanoTime() + "@gestion.local");
        owner.setPassword(passwordEncoder.encode("secreto123"));
        owner.setRole(Role.OWNER);
        owner.setActive(true);
        owner.setCommerce(commerce);
        return userRepository.saveAndFlush(owner);
    }
}
