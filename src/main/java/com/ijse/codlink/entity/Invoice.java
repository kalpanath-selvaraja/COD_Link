package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "shipment_id", nullable = false, unique = true)
    @ToString.Exclude
    private Shipment shipment;
}