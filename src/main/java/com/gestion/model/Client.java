package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "clients",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_clients_commerce_dni",
                columnNames = {"commerce_id", "dni"}
        )
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commerce_id", nullable = false)
    private Long commerceId;

    @Column
    private String name;

    @Column(name = "last_name")
    private String lastName;

    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column
    private String dni;

    @Column(name = "created_at")
    private LocalDate createdAt;
}
