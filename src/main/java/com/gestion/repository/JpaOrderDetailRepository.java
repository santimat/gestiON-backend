package com.gestion.repository;

import com.gestion.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderIdIn(List<Long> orderIds);
}
