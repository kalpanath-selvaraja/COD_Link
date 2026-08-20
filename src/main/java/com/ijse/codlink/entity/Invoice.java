package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    private BigDecimal amount;

    @OneToOne
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    @ToString.Exclude
    private Shipment shipment;
}