package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationBatchDTO {
    @jakarta.validation.constraints.Positive
    private Long batchId;
    @jakarta.validation.constraints.Positive
    private Long branchId;
    private BigDecimal total;
    private LocalDateTime reconciledAt;
}