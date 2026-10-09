package com.gestion.repository;

import com.gestion.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByCommerceId(Long commerceId);
}
