package com.gestion.repository;

import com.gestion.model.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCommerceRepository extends JpaRepository<Commerce, Long> {
    boolean existsByCuit(String cuit);
}
