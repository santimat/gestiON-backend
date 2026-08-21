package com.gestion.repository;

import com.gestion.model.SaleDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSaleDetailRepository extends JpaRepository<SaleDetail, Long> {
}
