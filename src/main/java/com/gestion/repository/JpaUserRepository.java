package com.gestion.repository;

import com.gestion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
