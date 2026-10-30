package com.gestion.repository;

import com.gestion.dto.project.SaleItemCount;
import com.gestion.model.SaleDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaSaleDetailRepository extends JpaRepository<SaleDetail, Long> {

    @EntityGraph(attributePaths = "product")
    List<SaleDetail> findAllBySaleId(Long saleId);

    @Query("""
            SELECT d.sale.id AS saleId, SUM(d.quantity) AS itemCount
            FROM SaleDetail d
            WHERE d.sale.id IN :saleIds
            GROUP BY d.sale.id
            """)
    List<SaleItemCount> countItemsBySaleIds(@Param("saleIds") List<Long> saleIds);

}
