package com.gestion.repository;

import com.gestion.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySaleId(Long saleId);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.sale s JOIN FETCH s.commerce c WHERE commerce.id = " +
            ":commerceId")
    Page<Transaction> findByCommerceId(@Param("commerceId") Long commerceId, Pageable pageable);

}