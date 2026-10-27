package com.gestion.repository;

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

    List<User> findAllByIdInAndCommerceIdAndRole(Collection<Long> ids, Long commerceId, Role role);

    // join fetch trae los commerce de los usuarios evitando el n+1
    @Query("SELECT u FROM User as u JOIN FETCH u.commerce WHERE u.role = :userRole")
    Page<User> findAllByRoleWithCommerce(@Param("userRole") Role userRole, Pageable pageable);

    Page<User> findAllByRole(Role role, Pageable pageable);
}
