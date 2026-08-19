package com.gestion.repository.sale;

import com.gestion.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSaleRepository extends JpaRepository<Sale, Long> {
}
