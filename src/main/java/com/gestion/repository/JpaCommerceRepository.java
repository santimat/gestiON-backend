package com.gestion.repository;

import com.gestion.model.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCommerceRepository extends JpaRepository<Commerce, Long> {
    boolean existsByCuit(String cuit);

    boolean existsByAddress(String address);

    // variantes que excluyen al propio comercio: al actualizar, el comercio ya tiene ese cuit
    // o esa direccion, asi que el chequeo tiene que ignorarlo.
    boolean existsByCuitAndIdNot(String cuit, Long id);

    boolean existsByAddressAndIdNot(String address, Long id);

    Long countByActive(boolean active);
}
