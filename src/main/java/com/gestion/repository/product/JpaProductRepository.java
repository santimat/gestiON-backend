package com.gestion.repository.product;

import com.gestion.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<Product,Long> {
}
