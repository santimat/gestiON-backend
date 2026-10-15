package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "clients")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class
Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long commerceId;  // agregue esto para traer los clientes del comercio que consulta nada mas

    @Column
    private String name;

    @Column(name = "last_name")
    private String lastName;

    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(unique = true)
    private String dni;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
