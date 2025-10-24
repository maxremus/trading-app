package com.tradingapp.tradingapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Customer customer;

    @ManyToOne(optional = false)
    private Product product;

    private int quantity;

    private BigDecimal totalPrice;

    private LocalDateTime createdOn;
}
