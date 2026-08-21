package com.gestion.model;

import jakarta.persistence.*;

@Entity
@Table
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Product product;

    private Double value;
}
