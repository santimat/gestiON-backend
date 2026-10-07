package com.gestion.repository;

import com.gestion.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
