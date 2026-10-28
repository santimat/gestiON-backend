package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commerce_id")
    private Commerce commerce;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 100, nullable = false)
    private String name;

    private String description;

    @Column(name = "image_name")
    private String imageName;

    @Column(nullable = false, name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false, name = "min_stock")
    private Integer minStock;

    @Column(nullable = false, name = "current_stock")
    private Integer currentStock;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "profit_multiplier")
    private Double profitMultiplier;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
