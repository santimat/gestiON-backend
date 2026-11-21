package com.gestion.repository;

import com.gestion.enums.TransactionType;
import com.gestion.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySaleId(Long saleId);

    @Query("SELECT t FROM Transaction t WHERE t.sale.commerce.id = :commerceId")
    Page<Transaction> findByCommerceId(@Param("commerceId") Long commerceId, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN t.sale s WHERE s.id = :saleId AND t.type = :type")
    List<Transaction> findBySaleIdAndType(@Param("saleId") Long saleId, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sale.id = :saleId AND t.type = com.gestion" +
            ".enums.TransactionType.PAYMENT")
    BigDecimal sumPaymentsBySaleId(@Param("saleId") Long saleId);

}