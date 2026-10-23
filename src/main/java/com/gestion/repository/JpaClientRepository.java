package com.gestion.repository;

import com.gestion.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaClientRepository extends JpaRepository<Client, Long> {

    boolean existsByDni(String dni);

    Optional<Client> findByDni(String dni);

    Page<Client> findByCommerceId(Long commerceId, Pageable pageable);

    Optional<Client> findByIdAndCommerceId(Long clientId, Long commerceId);

    boolean existsByDniAndCommerceId(String dni, Long commerceId);

    boolean existsByDniAndCommerceIdAndIdNot(String dni, Long commerceId, Long id); // para el update

    Optional<Client> findByDniAndCommerceId(String dni, Long commerceId);
}
