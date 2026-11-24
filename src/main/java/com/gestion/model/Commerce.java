package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "commerces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commerce {
    public static final double DEFAULT_PROFIT_MULTIPLIER = 1.0;
    public static final int DEFAULT_MIN_STOCK = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String address;

    @Column(nullable = false, unique = true, length = 12)
    private String cuit;

    @Column(unique = true)
    private String logoName;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "profit_multiplier", nullable = false)
    private Double profitMultiplier;

    @Column(name = "default_min_stock", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 3")
    private Integer defaultMinStock = DEFAULT_MIN_STOCK;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false)
    @UpdateTimestamp
    private Instant updatedAt;
}
