package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commerce_id")
    private Commerce commerce;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderDetail> details = new ArrayList<>();

    @Column
    private String description;

    @Column
    private BigDecimal total; // este total es la suma de los subtotales

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Para que el bucle sea limpio y la relación bidireccional no falle en la base de datos
    public void addDetail(OrderDetail detail) {
        this.details.add(detail);
        detail.setOrder(this);
    }
}
