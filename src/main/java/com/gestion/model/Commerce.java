package com.gestion.model;

import jakarta.persistence.*;

@Entity
@Table(name = "commerces")

public class Commerce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
