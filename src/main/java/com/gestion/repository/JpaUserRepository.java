package com.gestion.repository;

import com.gestion.dto.project.user.UserActiveStatus;
import com.gestion.enums.Role;
import com.gestion.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndCommerceIdAndRole(Long id, Long commerceId, Role role);

    // join fetch trae los commerce de los usuarios evitando el n+1
    @Query(
            value = """
                    SELECT u FROM User u
                    JOIN FETCH u.commerce c
                    WHERE u.role = :userRole
                      AND (:searchText IS NULL
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
                           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')))
                      AND (:active IS NULL OR c.active = :active)
                    """,
            countQuery = """
                    SELECT COUNT(u) FROM User u
                    JOIN u.commerce c
                    WHERE u.role = :userRole
                      AND (:searchText IS NULL
                           OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
                           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchText, '%')))
                      AND (:active IS NULL OR c.active = :active)
                    """
    )
    Page<User> findAllByRoleWithCommerce(@Param("userRole") Role userRole,
                                         @Param("searchText") String searchText,
                                         @Param("active") Boolean active,
                                         Pageable pageable);

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

    @Query("""
            SELECT new com.gestion.dto.project.user.UserActiveStatus(
                u.active,
                c.active
            )
            FROM User u
            LEFT JOIN u.commerce c
            WHERE u.id = :userId
            """)
    Optional<UserActiveStatus> findActiveStatusById(@Param("userId") Long userId);
}
