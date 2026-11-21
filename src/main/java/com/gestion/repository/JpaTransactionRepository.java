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

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sale.id = :saleId AND t.type = com.gestion" +
            ".enums.TransactionType.PAYMENT")
    BigDecimal sumPaymentsBySaleId(@Param("saleId") Long saleId);

}