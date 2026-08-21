package com.gestion.repository;

import com.gestion.model.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCommerceRepository extends JpaRepository<Commerce, Long> {
    Optional<Commerce> findByBusinessName(String businessName);
}
