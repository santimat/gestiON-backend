package com.gestion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private User user;

    private Commerce commerce;

    private Enum paymentMethod;

    private Float subtotal;

    private Double discount;

    private Float total;

    private Date createdAt;

    private String observations;

    private Enum state;
}
