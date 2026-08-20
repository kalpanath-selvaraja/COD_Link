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
@Table(name = "cod_collections")
public class CodCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long collectionId;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false, unique = true)
    @ToString.Exclude
    private Parcel parcel;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    @ToString.Exclude
    private Courier courier;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @ToString.Exclude
    private ReconciliationBatch reconciliationBatch;
}