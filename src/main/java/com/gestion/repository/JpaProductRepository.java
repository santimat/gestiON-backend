package com.gestion.repository;

import com.gestion.dto.project.category.CategoryProductCount;
import com.gestion.dto.project.offer.OfferProductCount;
import com.gestion.dto.response.product.ProductLowStockResponse;
import com.gestion.model.Category;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = {"category", "offer"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    Optional<Product> findByIdAndCommerceId(Long id, Long commerceId);

    boolean existsByOfferIdAndCommerceId(Long offerId, Long commerceId);

    List<Product> findByIdInAndCommerceId(List<Long> ids, Long commerceId);

    List<Product> findByOfferIdAndCommerceId(Long offerId, Long commerceId);

    @Query("""
            SELECT p.id FROM Product p
            WHERE p.offer.id = :offerId
              AND p.commerce.id = :commerceId
            """)
    List<Long> findIdsByOfferIdAndCommerceId(@Param("offerId") Long offerId,
                                             @Param("commerceId") Long commerceId);

    default Page<Product> findAllWithFilters(Long commerceId, Long categoryId, String searchText,
                                             Boolean active, String stockStatus, Pageable pageable) {
        return findAllWithFilters(commerceId, categoryId, searchText, active, stockStatus, null, pageable);
    }

    default Page<Product> findAllWithFilters(Long commerceId, Long categoryId, String searchText,
                                             Boolean active, String stockStatus, Long prioritizeOfferId,
                                             Pageable pageable) {
        Specification<Product> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("commerce").get("id"), commerceId));

            Join<Product, Category> categoryJoin = null;
            if (categoryId != null || searchText != null) {
                categoryJoin = root.join("category", JoinType.INNER);
            }

            if (categoryId != null) {
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            }

            if (searchText != null) {
                String pattern = "%" + searchText.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(categoryJoin.get("name")), pattern)
                ));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if ("LOW_STOCK".equals(stockStatus)) {
                predicates.add(cb.and(
                        cb.greaterThan(root.get("currentStock"), 0),
                        cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minStock"))
                ));
            } else if ("WITHOUT_STOCK".equals(stockStatus)) {
                predicates.add(cb.equal(root.get("currentStock"), 0));
            }

            boolean isCountQuery = query.getResultType() == Long.class || query.getResultType() == long.class;
            if (prioritizeOfferId != null && !isCountQuery) {
                Join<Product, Offer> offerJoin = root.join("offer", JoinType.LEFT);
                Expression<Integer> priorityOrder = cb.<Integer>selectCase()
                        .when(cb.equal(offerJoin.get("id"), prioritizeOfferId), 0)
                        .otherwise(1);

                List<Order> orders = new ArrayList<>();
                orders.add(cb.asc(priorityOrder));
                orders.addAll(QueryUtils.toOrders(pageable.getSort(), root, cb));
                query.orderBy(orders);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable effectivePageable = prioritizeOfferId != null
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : pageable;

        return findAll(specification, effectivePageable);
    }

    @Query("""
            SELECT p.category.id AS categoryId, COUNT(p) AS productCount
            FROM Product p
            WHERE p.commerce.id = :commerceId
              AND p.category.id IN :categoryIds
            GROUP BY p.category.id
            """)
    List<CategoryProductCount> countByCategoryIds(@Param("commerceId") Long commerceId,
                                                  @Param("categoryIds") List<Long> categoryIds);

    @Query("""
            SELECT p.offer.id AS offerId, COUNT(p) AS productCount
            FROM Product p
            WHERE p.commerce.id = :commerceId
              AND p.offer.id IN :offerIds
            GROUP BY p.offer.id
            """)
    List<OfferProductCount> countByOfferIds(@Param("commerceId") Long commerceId,
                                            @Param("offerIds") List<Long> offerIds);

    @Query("""
            SELECT new com.gestion.dto.response.product.ProductLowStockResponse(
                p.id,
                p.name,
                p.minStock,
                p.currentStock
            )
            FROM Product p
            WHERE p.commerce.id = :commerceId
              AND p.active = true
              AND p.currentStock <= p.minStock
            """)
    List<ProductLowStockResponse> findLowStockProductsByCommerceId(@Param("commerceId") Long commerceId);

}
