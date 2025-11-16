package com.tradingapp.tradingapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Size(min = 9, max = 9, message = "ЕИК must be between 9 digits")
    private String eik;

    @Column(nullable = false, unique = true)
    private String email;

    private String address;
}
