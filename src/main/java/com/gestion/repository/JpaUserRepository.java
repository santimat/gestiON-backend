package com.gestion.repository;

import com.gestion.dto.project.user.UserSessionSnapshot;
import com.gestion.enums.Role;
import com.gestion.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndCommerceIdAndRole(Long id, Long commerceId, Role role);

    // owners de una pagina de comercios, en una sola query (el listado pagina sobre Commerce)
    List<User> findAllByRoleAndCommerceIdIn(Role role, Collection<Long> commerceIds);

    // join fetch trae el commerce del cajero evitando el n+1
    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.commerce c
            WHERE u.role = com.gestion.enums.Role.CASHIER
              AND c.id = :commerceId
              AND (:searchText IS NULL
                   OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')))
            """)
    Page<User> findAllCashiersWithFilters(@Param("commerceId") Long commerceId,
                                          @Param("searchText") String searchText,
                                          Pageable pageable);

    Page<User> findAllBySellPointIdAndCommerceId(Long sellPointId, Long commerceId, Pageable pageable);

    // snapshot de la sesion que JwtFilter lee en cada request: proyeccion minima (sin cargar
    // entidades) con los datos que el principal necesita y que pueden cambiar despues del login.
    @Query("""
            SELECT new com.gestion.dto.project.user.UserSessionSnapshot(
                u.id,
                u.email,
                u.name,
                u.role,
                u.active,
                c.id,
                c.active,
                sp.id
            )
            FROM User u
            LEFT JOIN u.commerce c
            LEFT JOIN u.sellPoint sp
            WHERE u.id = :userId
            """)
    Optional<UserSessionSnapshot> findSessionSnapshotById(@Param("userId") Long userId);
}
