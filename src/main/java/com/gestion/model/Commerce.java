package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "commerces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Commerce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "business_name")
    private String businessName;

    @Column(nullable = false, unique = true, length = 100)
    private String address;

    @Column(nullable = false, unique = true, length = 12)
    private String cuit;

    @Column(unique = true)
    private String logoName;

    private boolean active;

    private double profitPercetange;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
