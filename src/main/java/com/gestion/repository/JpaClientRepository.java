package com.gestion.repository;

import com.gestion.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaClientRepository extends JpaRepository<Client, Long> {

    boolean existsByDniAndCommerceId(String dni, Long commerceId);

    Optional<Client> findByDniAndCommerceId(String dni, Long commerceId);

    boolean existsByDniAndCommerceIdAndIdNot(String dni, Long commerceId, Long id);

    Optional<Client> findByIdAndCommerceId(Long id, Long commerceId);

    @Query("""
            SELECT c FROM Client c
            WHERE c.commerce.id = :commerceId
              AND (:searchText IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :searchText, '%')))
            """)
    Page<Client> findAllWithFilters(@Param("commerceId") Long commerceId,
                                    @Param("searchText") String searchText,
                                    Pageable pageable);
}
