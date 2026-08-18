package com.gestion.on.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "commerces")
public class Commerce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
