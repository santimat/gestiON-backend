package com.gestion.repository;

import com.gestion.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaClientRepository extends JpaRepository<Client, Long> {

    boolean existsByDniAndCommerceId(String dni, Long commerceId);

    Optional<Client> findByDniAndCommerceId(String dni, Long commerceId);

    boolean existsByDniAndCommerceIdAndIdNot(String dni, Long commerceId, Long id);

    Optional<Client> findByIdAndCommerceId(Long id, Long commerceId);

    Page<Client> findAllByCommerceId(Long commerceId, Pageable pageable);
}
