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
@Table(name = "reconciliation_batches")
public class ReconciliationBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchId;

    private BigDecimal total;
    private LocalDateTime reconciledAt;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    @ToString.Exclude
    private Branch branch;
}