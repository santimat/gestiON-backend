package com.gestion.repository.commerce;

import com.gestion.model.Commerce;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCommerceRepository extends JpaRepository<Commerce, Long> {
}
