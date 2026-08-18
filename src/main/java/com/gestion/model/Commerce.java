package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(unique = true, nullable = false, name = "business_name")
    private String businessName;

    @Column(nullable = false, unique = true, length = 100)
    private String address;

    @Column(nullable = false, unique = true, length = 100, name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String cuit;

    @Column(unique = true)
    private String logoUrl;
}
