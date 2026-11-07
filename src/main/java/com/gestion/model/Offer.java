package com.gestion.model;

import com.gestion.enums.DiscountType;
import com.gestion.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Offer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commerce_id")
    private Commerce commerce;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    // tabla auxiliar para guardar metodos de pago que tienen oferta
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "offer_payment_methods", joinColumns = @JoinColumn(name = "offer_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private Set<PaymentMethod> paymentMethods = new HashSet<>();
}
