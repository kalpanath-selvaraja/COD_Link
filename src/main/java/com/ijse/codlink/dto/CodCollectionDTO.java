package com.ijse.codlink.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodCollectionDTO {
    @jakarta.validation.constraints.Positive
    private Long collectionId;
    @NotNull(message = "parcelId is required")
    @jakarta.validation.constraints.Positive
    private Long parcelId;
    @jakarta.validation.constraints.Positive
    private Long courierId;
    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than zero")
    private BigDecimal amount;
    @jakarta.validation.constraints.Positive
    private Long batchId;
}